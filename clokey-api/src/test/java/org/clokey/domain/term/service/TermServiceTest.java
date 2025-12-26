package org.clokey.domain.term.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.BDDMockito.given;

import java.util.List;
import org.assertj.core.groups.Tuple;
import org.clokey.IntegrationTest;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.domain.term.dto.request.TermAgreeRequest;
import org.clokey.domain.term.dto.response.MyOptionalTermResponse;
import org.clokey.domain.term.dto.response.TermListResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
    class 전체_약관_조회_요청_시 {

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
        void 유효한_요청이면_전체_약관을_반환한다() {
            // when
            TermListResponse response = termService.getTerms();

            // then
            assertThat(response.payloads())
                    .extracting("termId", "title", "body", "optional")
                    .containsExactly(
                            tuple(1L, "testTerm1", "testContent1", false),
                            tuple(2L, "testTerm2", "testContent2", false),
                            tuple(3L, "testTerm3", "testContent3", false),
                            tuple(4L, "testTerm4", "testContent4", true),
                            tuple(5L, "testTerm5", "testContent5", true));
        }
    }

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

            assertThat(memberTerms)
                    .extracting("member.id", "term.id", "agreed")
                    .containsExactly(
                            tuple(1L, 1L, true),
                            tuple(1L, 2L, true),
                            tuple(1L, 3L, true),
                            tuple(1L, 4L, false),
                            tuple(1L, 5L, true));
        }

        @Test
        void 모든_약관에_대한_정보를_포함하지_않으면_예외가_발생한다() {
            // given
            TermAgreeRequest request =
                    new TermAgreeRequest(
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

    @Nested
    class 나의_선택_약관_정보_조회를_요청할_때 {

        @BeforeEach
        void setUp() {
            Member member1 =
                    Member.createMember(
                            "testEmail",
                            "testClokeyId1",
                            "testNickName1",
                            OauthInfo.createOauthInfo("testOauthId", OauthProvider.KAKAO));
            Member member2 =
                    Member.createMember(
                            "testEmail2",
                            "testClokeyId2",
                            "testNickName2",
                            OauthInfo.createOauthInfo("testOauthId", OauthProvider.KAKAO));
            memberRepository.saveAll(List.of(member1, member2));
            given(memberUtil.getCurrentMember()).willReturn(member1);

            Term term1 = Term.createTerm("testTerm1", "testContent1", false);
            Term term2 = Term.createTerm("testTerm2", "testContent2", false);
            Term term3 = Term.createTerm("testTerm3", "testContent3", false);
            Term term4 = Term.createTerm("testTerm4", "testContent4", true);
            Term term5 = Term.createTerm("testTerm5", "testContent5", true);
            termRepository.saveAll(List.of(term1, term2, term3, term4, term5));

            MemberTerm memberTerm1 = MemberTerm.createMemberTerm(member1, term4, true);
            MemberTerm memberTerm2 = MemberTerm.createMemberTerm(member1, term5, true);
            memberTermRepository.saveAll(List.of(memberTerm1, memberTerm2));
        }

        @Test
        void 유효한_요청이면_나의_선택_약관_정보를_반환한다() {
            // when
            MyOptionalTermResponse response = termService.getMyOptionalTerms();

            // then
            assertThat(response.payloads())
                    .extracting("termId", "agreed")
                    .containsExactly(tuple(4L, true), Tuple.tuple(5L, true));
        }

        @Test
        void 약관_동의_절차를_수행하지_않은_회원이_요청할_경우_예외가_발생한다() {
            // given
            Member skippedTermProcess = memberRepository.findById(2L).orElseThrow();
            given(memberUtil.getCurrentMember()).willReturn(skippedTermProcess);

            // when & then
            assertThatThrownBy(() -> termService.getMyOptionalTerms())
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(TermErrorCode.MEMBER_SKIPPED_TERM_AGREEMENT.getMessage());
        }
    }

    @Nested
    class 나의_선택_약관_정보_수정을_요청할_때 {

        @BeforeEach
        void setUp() {
            Member member1 =
                    Member.createMember(
                            "testEmail",
                            "testClokeyId1",
                            "testNickName1",
                            OauthInfo.createOauthInfo("testOauthId", OauthProvider.KAKAO));
            Member member2 =
                    Member.createMember(
                            "testEmail2",
                            "testClokeyId2",
                            "testNickName2",
                            OauthInfo.createOauthInfo("testOauthId", OauthProvider.KAKAO));
            memberRepository.saveAll(List.of(member1, member2));
            given(memberUtil.getCurrentMember()).willReturn(member1);

            Term term1 = Term.createTerm("testTerm1", "testContent1", false);
            Term term2 = Term.createTerm("testTerm2", "testContent2", false);
            Term term3 = Term.createTerm("testTerm3", "testContent3", false);
            Term term4 = Term.createTerm("testTerm4", "testContent4", true);
            Term term5 = Term.createTerm("testTerm5", "testContent5", true);
            termRepository.saveAll(List.of(term1, term2, term3, term4, term5));

            MemberTerm memberTerm1 = MemberTerm.createMemberTerm(member1, term4, true);
            MemberTerm memberTerm2 = MemberTerm.createMemberTerm(member1, term5, false);
            memberTermRepository.saveAll(List.of(memberTerm1, memberTerm2));
        }

        @ParameterizedTest
        @CsvSource({"4, false", "5, true"})
        void 유효한_요청이면_선택_약관_정보를_수정한다(Long termId, boolean expectedAgreed) {
            // when
            termService.toggleMyOptionalTerms(termId);

            // then
            MemberTerm memberTerm =
                    memberTermRepository.findByMemberIdAndTermId(1L, termId).orElseThrow();
            assertThat(memberTerm.isAgreed()).isEqualTo(expectedAgreed);
        }

        @Test
        void 선택_약관이_아닌_약관의_ID값을_입력할_경우_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> termService.toggleMyOptionalTerms(3L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(TermErrorCode.NOT_OPTIONAL_TERM.getMessage());
        }

        @Test
        void 약관_동의_절차를_수행하지_않은_회원이_요청할_경우_예외가_발생한다() {
            // given
            Member skippedTermProcess = memberRepository.findById(2L).orElseThrow();
            given(memberUtil.getCurrentMember()).willReturn(skippedTermProcess);

            // when & then
            assertThatThrownBy(() -> termService.toggleMyOptionalTerms(4L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(TermErrorCode.MEMBER_SKIPPED_TERM_AGREEMENT.getMessage());
        }
    }
}
