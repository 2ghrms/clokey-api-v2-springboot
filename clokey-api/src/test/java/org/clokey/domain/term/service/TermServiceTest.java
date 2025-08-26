package org.clokey.domain.term.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import java.util.List;
import org.clokey.IntegrationTest;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.domain.term.dto.TermAgreeRequest;
import org.clokey.domain.term.exception.TermErrorCode;
import org.clokey.domain.term.repository.MemberTermRepository;
import org.clokey.domain.term.repository.TermRepository;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.util.MemberUtil;
import org.clokey.member.entity.Member;
import org.clokey.member.entity.OauthInfo;
import org.clokey.member.enums.OauthProvider;
import org.clokey.term.entity.MemberTerm;
import org.clokey.term.entity.Term;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

class TermServiceTest extends IntegrationTest {

    @Autowired MemberRepository memberRepository;
    @Autowired TermRepository termRepository;
    @Autowired MemberTermRepository memberTermRepository;
    @Autowired TermService termService;

    @MockitoBean private MemberUtil memberUtil;

    @Nested
    class 약관에_동의할_때 {

        @BeforeEach
        void setUp() {
            Member member =
                    Member.createMember(
                            "testEmail",
                            "testClokeyId",
                            "testNickName",
                            OauthInfo.createOauthInfo("testOauthId", OauthProvider.KAKAO));
            Member savedMember = memberRepository.save(member);
            given(memberUtil.getCurrentMember()).willReturn(savedMember);

            Term term1 = Term.createTerm("testTerm1", "testContent1", false);
            Term term2 = Term.createTerm("testTerm2", "testContent2", false);
            Term term3 = Term.createTerm("testTerm3", "testContent3", false);
            Term term4 = Term.createTerm("testTerm4", "testContent4", true);
            Term term5 = Term.createTerm("testTerm5", "testContent5", true);
            termRepository.saveAll(List.of(term1, term2, term3, term4, term5));
        }

        @Test
        @Transactional
        void 유효한_요청이면_약관_정보와_Device_Token을_저장한다() {
            // given
            TermAgreeRequest request =
                    new TermAgreeRequest(
                            "testDeviceToken",
                            List.of(
                                    new TermAgreeRequest.Payload(1L, true),
                                    new TermAgreeRequest.Payload(2L, true),
                                    new TermAgreeRequest.Payload(3L, true),
                                    new TermAgreeRequest.Payload(4L, false),
                                    new TermAgreeRequest.Payload(5L, true)));

            // when
            termService.agreeTerm(request);

            // then
            Member member = memberRepository.findById(1L).orElseThrow();
            List<MemberTerm> memberTerms =
                    memberTermRepository.findAllById(List.of(1L, 2L, 3L, 4L, 5L));
            Assertions.assertAll(
                    () -> assertThat(member.getDeviceToken()).isEqualTo("testDeviceToken"),
                    () ->
                            assertThat(memberTerms)
                                    .extracting("member.id", "term.id", "agreed")
                                    .containsExactly(
                                            tuple(1L, 1L, true),
                                            tuple(1L, 2L, true),
                                            tuple(1L, 3L, true),
                                            tuple(1L, 4L, false),
                                            tuple(1L, 5L, true)));
        }

        @Test
        void 모든_약관에_대한_정보를_포함하지_않으면_예외가_발생한다() {
            // given
            TermAgreeRequest request =
                    new TermAgreeRequest(
                            "testDeviceToken",
                            List.of(
                                    new TermAgreeRequest.Payload(1L, true),
                                    new TermAgreeRequest.Payload(2L, true),
                                    new TermAgreeRequest.Payload(3L, true),
                                    new TermAgreeRequest.Payload(4L, false)));

            // when
            assertThatThrownBy(() -> termService.agreeTerm(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(TermErrorCode.TERMS_MISMATCH.getMessage());
        }

        @Test
        void 필수_약관에_동의하지_않으면_예외가_발생한다() {
            // given
            TermAgreeRequest request =
                    new TermAgreeRequest(
                            "testDeviceToken",
                            List.of(
                                    new TermAgreeRequest.Payload(1L, true),
                                    new TermAgreeRequest.Payload(2L, true),
                                    new TermAgreeRequest.Payload(3L, false),
                                    new TermAgreeRequest.Payload(4L, false),
                                    new TermAgreeRequest.Payload(5L, false)));

            // when
            assertThatThrownBy(() -> termService.agreeTerm(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(TermErrorCode.NON_OPTIONAL_NOT_AGREED.getMessage());
        }
    }
}
