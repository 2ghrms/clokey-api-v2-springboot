package org.clokey.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.clokey.IntegrationTest;
import org.clokey.domain.auth.dto.response.UserStatusResponse;
import org.clokey.domain.auth.enums.RegisterStatus;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.domain.term.repository.MemberTermRepository;
import org.clokey.domain.term.repository.TermRepository;
import org.clokey.global.util.MemberUtil;
import org.clokey.member.entity.Member;
import org.clokey.member.entity.OauthInfo;
import org.clokey.member.enums.OauthProvider;
import org.clokey.term.entity.MemberTerm;
import org.clokey.term.entity.Term;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class AuthServiceTest extends IntegrationTest {

    @Autowired AuthService authService;
    @Autowired MemberRepository memberRepository;
    @Autowired TermRepository termRepository;
    @Autowired MemberTermRepository memberTermRepository;

    @MockitoBean MemberUtil memberUtil;

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

            MemberTerm memberTerm = MemberTerm.createMemberTerm(member, term);
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
}
