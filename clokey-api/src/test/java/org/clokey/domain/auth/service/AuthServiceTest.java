package org.clokey.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.clokey.IntegrationTest;
import org.clokey.RedisCleaner;
import org.clokey.auth.entity.RefreshToken;
import org.clokey.domain.auth.dto.AccessTokenDto;
import org.clokey.domain.auth.dto.RefreshTokenDto;
import org.clokey.domain.auth.dto.request.DeviceTokenRenewRequest;
import org.clokey.domain.auth.dto.request.TokenReissueRequest;
import org.clokey.domain.auth.dto.response.TokenResponse;
import org.clokey.domain.auth.dto.response.UserStatusResponse;
import org.clokey.domain.auth.enums.RegisterStatus;
import org.clokey.domain.auth.exception.AuthErrorCode;
import org.clokey.domain.auth.repository.RefreshTokenRepository;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.domain.term.repository.MemberTermRepository;
import org.clokey.domain.term.repository.TermRepository;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.util.MemberUtil;
import org.clokey.member.entity.Member;
import org.clokey.member.entity.OauthInfo;
import org.clokey.member.enums.MemberRole;
import org.clokey.member.enums.OauthProvider;
import org.clokey.term.entity.MemberTerm;
import org.clokey.term.entity.Term;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

class AuthServiceTest extends IntegrationTest {

    @Autowired private RedisCleaner redisCleaner;

    @Autowired private AuthService authService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private TermRepository termRepository;
    @Autowired private MemberTermRepository memberTermRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;

    @MockitoBean private JwtTokenService jwtTokenService;
    @MockitoBean private MemberUtil memberUtil;

    @Nested
    class 유저의_상태를_확인할_때 {

        @BeforeEach
        void setUp() {
            Member member =
                    Member.createMember(
                            "testEmail",
                            "testClokeyId",
                            "testNickName",
                            OauthInfo.createOauthInfo("testOauthId", OauthProvider.KAKAO));

            memberRepository.save(member);
            given(memberUtil.getCurrentMember()).willReturn(member);

            Term term = Term.createTerm("testTitle", "testBody", false);
            termRepository.save(term);
        }

        @Test
        void 약관에_동의한_유저의_경우_REGISTERED를_반환한다() {
            // given
            Member member = memberRepository.findById(1L).orElseThrow();
            Term term = termRepository.findById(1L).orElseThrow();

            MemberTerm memberTerm = MemberTerm.createMemberTerm(member, term, true);
            memberTermRepository.save(memberTerm);

            // when
            UserStatusResponse response = authService.getUserStatus();

            // then
            assertThat(response.registerStatus()).isEqualTo(RegisterStatus.REGISTERED);
        }

        @Test
        void 약관에_동의하지_않은_유저의_경우_NOT_AGREED를_반환한다() {
            // when
            UserStatusResponse response = authService.getUserStatus();

            // then
            assertThat(response.registerStatus()).isEqualTo(RegisterStatus.NOT_AGREED);
        }
    }

    @Nested
    class 디바이스_토큰을_갱신할_때 {

        @BeforeEach
        void setUp() {
            Member member =
                    Member.createMember(
                            "testEmail",
                            "testClokeyId",
                            "testNickName",
                            OauthInfo.createOauthInfo("testOauthId", OauthProvider.KAKAO));
            member.updateDeviceToken("testDeviceToken");

            memberRepository.save(member);
            given(memberUtil.getCurrentMember()).willReturn(member);
        }

        @Test
        @Transactional
        void 유효한_요청이면_Device_Token을_갱신한다() {
            // given
            DeviceTokenRenewRequest request = new DeviceTokenRenewRequest("newDeviceToken");

            // when
            authService.renewDeviceToken(request);

            // then
            assertThat(memberRepository.findById(1L).orElseThrow().getDeviceToken())
                    .isEqualTo("newDeviceToken");
        }
    }

    @Nested
    class 토큰_재발급을_요청할_때 {

        @BeforeEach
        void setUp() {
            Member member =
                    Member.createMember(
                            "testEmail",
                            "testClokeyId",
                            "testNickName",
                            OauthInfo.createOauthInfo("testOauthId", OauthProvider.KAKAO));
            memberRepository.save(member);
        }

        @Test
        void 유효한_리프레시_토큰이면_새로운_엑세스_토큰과_리프레시_토큰을_반환한다() {
            // given
            RefreshTokenDto oldRefreshTokenDto =
                    RefreshTokenDto.of(1L, "fake-old-register-token", 604800L);
            RefreshTokenDto newRefreshTokenDto =
                    RefreshTokenDto.of(1L, "fake-new-refresh-token", 604800L);
            AccessTokenDto newAccessTokenDto =
                    AccessTokenDto.of(1L, MemberRole.USER, "fake-new-access-token");

            given(jwtTokenService.retrieveRefreshToken(anyString())).willReturn(oldRefreshTokenDto);
            given(jwtTokenService.reissueRefreshToken(oldRefreshTokenDto))
                    .willReturn(newRefreshTokenDto);
            given(jwtTokenService.reissueAccessToken(any())).willReturn(newAccessTokenDto);

            // when
            TokenResponse response =
                    authService.reissueTokens(new TokenReissueRequest("testRefreshToken"));

            // then
            assertThat(response)
                    .extracting("accessToken", "refreshToken")
                    .containsExactly("fake-new-access-token", "fake-new-refresh-token");
        }

        @Test
        void 만료된_리프레시_토큰이면_예외가_발생한다() {
            // given
            assertThatThrownBy(
                            () ->
                                    authService.reissueTokens(
                                            new TokenReissueRequest("testRefreshToken")))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(AuthErrorCode.INVALID_REFRESH_TOKEN.getMessage());
            verify(jwtTokenService, times(1)).retrieveRefreshToken(anyString());
        }
    }

    @Nested
    class 로그아웃할_때 {

        @BeforeEach
        void setUp() {
            Member member =
                    Member.createMember(
                            "testEmail",
                            "testClokeyId",
                            "testNickName",
                            OauthInfo.createOauthInfo("testOauthId", OauthProvider.KAKAO));

            memberRepository.save(member);
            given(memberUtil.getCurrentMember()).willReturn(member);
        }

        @Test
        void Redis에_저장된_리프레시_토큰이_삭제된다() {
            // given
            RefreshToken refreshToken =
                    RefreshToken.builder().memberId(1L).token("testRefreshToken").build();
            refreshTokenRepository.save(refreshToken);

            // when
            authService.logoutUser();

            // then
            assertThat(refreshTokenRepository.findById(1L).isEmpty()).isTrue();
        }
    }
}
