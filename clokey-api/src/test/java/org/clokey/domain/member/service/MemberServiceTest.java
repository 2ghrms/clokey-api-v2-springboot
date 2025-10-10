package org.clokey.domain.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import java.awt.*;
import java.util.List;
import org.clokey.IntegrationTest;
import org.clokey.TransactionUtil;
import org.clokey.domain.member.dto.request.DuplicatedIdCheckRequest;
import org.clokey.domain.member.dto.request.ProfileUpdateRequest;
import org.clokey.domain.member.dto.response.BlockedMemberResponse;
import org.clokey.domain.member.dto.response.MyselfCheckResponse;
import org.clokey.domain.member.exception.MemberErrorCode;
import org.clokey.domain.member.repository.BlockRepository;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.paging.SortDirection;
import org.clokey.global.util.MemberUtil;
import org.clokey.member.entity.Block;
import org.clokey.member.entity.Member;
import org.clokey.member.entity.OauthInfo;
import org.clokey.member.enums.MemberStatus;
import org.clokey.member.enums.OauthProvider;
import org.clokey.member.enums.Visibility;
import org.clokey.response.SliceResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

class MemberServiceTest extends IntegrationTest {

    @Autowired private MemberService memberService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private BlockRepository blockRepository;

    @Autowired private TransactionUtil transactionUtil;
    @MockitoBean private MemberUtil memberUtil;

    @Nested
    class 프로필을_수정할_때 {

        @BeforeEach
        void setUp() {
            Member member =
                    Member.createMember(
                            "testEmail",
                            "oldClokeyId",
                            "oldNickname",
                            OauthInfo.createOauthInfo("testOauthId", OauthProvider.KAKAO));

            memberRepository.save(member);
            given(memberUtil.getCurrentMember()).willReturn(member);
        }

        @Test
        @Transactional
        void 유효한_요청이면_프로필을_수정한다() {
            // given
            ProfileUpdateRequest request =
                    new ProfileUpdateRequest(
                            "newNickname",
                            "newClokeyId",
                            "newBio",
                            Visibility.PUBLIC,
                            "https://img.example.com/profile.jpg",
                            "https://img.example.com/back.jpg");

            // when
            memberService.updateProfile(request);

            // then
            assertThat(memberRepository.findById(1L).orElseThrow())
                    .extracting(
                            "nickname",
                            "clokeyId",
                            "bio",
                            "profileImageUrl",
                            "profileBackImageUrl",
                            "visibility")
                    .containsExactly(
                            "newNickname",
                            "newClokeyId",
                            "newBio",
                            "https://img.example.com/profile.jpg",
                            "https://img.example.com/back.jpg",
                            Visibility.PUBLIC);
        }

        @Test
        void 밴된_회원이_PUBLIC으로_변경하려면_예외가_발생한다() {
            // given
            Member current = memberUtil.getCurrentMember();
            current.updateMemberStatus(MemberStatus.BANNED);
            memberRepository.save(current);
            ProfileUpdateRequest request =
                    new ProfileUpdateRequest(
                            "testNickname",
                            "testClokeyId",
                            "testBio",
                            Visibility.PUBLIC,
                            "profile.jpg",
                            "back.jpg");

            // when & then
            assertThatThrownBy(() -> memberService.updateProfile(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(MemberErrorCode.BANNED_MEMBER_TO_PUBLIC.getMessage());
        }
    }

    @Nested
    class 아이디_중복을_확인할_때 {

        @BeforeEach
        void setUp() {
            Member member1 =
                    Member.createMember(
                            "testEmail1",
                            "testClokeyId1",
                            "testNickname1",
                            OauthInfo.createOauthInfo("testOauthId", OauthProvider.KAKAO));
            Member member2 =
                    Member.createMember(
                            "testEmail2",
                            "testClokeyId2",
                            "testNickname2",
                            OauthInfo.createOauthInfo("testOauthId", OauthProvider.KAKAO));

            memberRepository.saveAll(List.of(member1, member2));
            given(memberUtil.getCurrentMember()).willReturn(member1);
        }

        @ParameterizedTest
        @ValueSource(strings = {"testClokeyId1", "distinctId1", "distinctId2"})
        void 현재_ID_또는_중복되지_않는_ID를_입력하면_false를_반환한다(String clokeyId) {
            // given
            DuplicatedIdCheckRequest request = new DuplicatedIdCheckRequest(clokeyId);

            // when& then
            assertThat(memberService.checkDuplicateClokeyId(request).duplicated()).isFalse();
        }

        @Test
        void 중복되는_ID를_입력한_경우_true를_반환한다() {
            // given
            DuplicatedIdCheckRequest request = new DuplicatedIdCheckRequest("testClokeyId2");

            // when& then
            assertThat(memberService.checkDuplicateClokeyId(request).duplicated()).isTrue();
        }
    }

    @Nested
    class 차단을_토글할_때 {

        @BeforeEach
        void setUp() {
            Member member1 =
                    Member.createMember(
                            "testEmail1",
                            "testClokeyId1",
                            "testNickname1",
                            OauthInfo.createOauthInfo("testOauthId", OauthProvider.KAKAO));
            Member member2 =
                    Member.createMember(
                            "testEmail2",
                            "testClokeyId2",
                            "testNickname2",
                            OauthInfo.createOauthInfo("testOauthId", OauthProvider.KAKAO));

            memberRepository.saveAll(List.of(member1, member2));
            given(memberUtil.getCurrentMember()).willReturn(member1);
        }

        @Test
        void 차단_상태가_아니라면_차단한다() {
            // when
            memberService.toggleBlockStatus(2L);

            // then
            assertThat(blockRepository.findById(1L).orElseThrow())
                    .extracting("blocker.id", "blocked.id")
                    .containsExactly(1L, 2L);
        }

        @Test
        void 차단_상태라면_차단을_해제한다() {
            // given
            Member blocker = memberRepository.findById(1L).orElseThrow();
            Member blocked = memberRepository.findById(2L).orElseThrow();
            Block block = Block.createBlock(blocker, blocked);
            blockRepository.save(block);

            // when
            memberService.toggleBlockStatus(2L);

            // then
            assertThat(
                            blockRepository.findByBlockerIdAndBlockedId(
                                    blocker.getId(), blocked.getId()))
                    .isNotPresent();
        }

        @Test
        void 자기_자신을_차단하면_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> memberService.toggleBlockStatus(1L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(MemberErrorCode.SELF_BLOCK_UNAVAILABLE.getMessage());
        }

        @Test
        void 차단_대상이_존재하지_않으면_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> memberService.toggleBlockStatus(999L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 본인인지_확인할_때 {

        @BeforeEach
        void setUp() {
            Member member1 =
                    Member.createMember(
                            "testEmail1",
                            "testClokeyId1",
                            "testNickname1",
                            OauthInfo.createOauthInfo("testOauthId", OauthProvider.KAKAO));
            memberRepository.save(member1);

            given(memberUtil.getCurrentMember()).willReturn(member1);
        }

        @Test
        void 유효한_요청이면_본인_여부를_반환한다() {
            // given
            String clokeyId = "testClokeyId1";

            // when
            MyselfCheckResponse response = memberService.checkIsMyself(clokeyId);

            // then
            assertThat(response.isMyself()).isEqualTo(true);
        }

        @Test
        void 클로키_아이디가_존재하지_않으면_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> memberService.checkIsMyself("WrongId"))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage("해당 클로키 아이디를 찾을 수 없습니다.");
        }
    }

    @Nested
    class 차단_멤버를_조회할_때 {

        @BeforeEach
        void setUp() {
            Member member1 =
                    Member.createMember(
                            "testEmail1",
                            "testCodiveId1",
                            "testNickName1",
                            OauthInfo.createOauthInfo("testOauthId1", OauthProvider.KAKAO));
            Member member2 =
                    Member.createMember(
                            "testEmail2",
                            "testCodiveId2",
                            "testNickName2",
                            OauthInfo.createOauthInfo("testOauthId2", OauthProvider.KAKAO));
            Member member3 =
                    Member.createMember(
                            "testEmail3",
                            "testCodiveId3",
                            "testNickName3",
                            OauthInfo.createOauthInfo("testOauthId3", OauthProvider.KAKAO));
            memberRepository.saveAll(List.of(member1, member2, member3));
            given(memberUtil.getCurrentMember()).willReturn(member1);

            Block block1 = Block.createBlock(member1, member2);
            Block block2 = Block.createBlock(member1, member3);
            Block block3 = Block.createBlock(member2, member3);

            blockRepository.saveAll(List.of(block1, block2, block3));
        }

        @Test
        void 유효한_요청이면_차단_멤버_목록을_반환한다() {
            // when
            SliceResponse<BlockedMemberResponse> response =
                    memberService.getBlockedMembers(null, 5, SortDirection.DESC);

            // then
            assertThat(response.content())
                    .extracting("codiveId")
                    .containsExactly("testCodiveId3", "testCodiveId2");
        }

        @Test
        void 정렬_조건이_DESC이면_BlockId를_내림차순으로_조회한다() {
            // when
            SliceResponse<BlockedMemberResponse> response =
                    memberService.getBlockedMembers(null, 5, SortDirection.DESC);

            // then
            assertThat(response.content()).extracting("blockId").containsExactly(2L, 1L);
        }

        @Test
        void 정렬_조건이_ASC이면_BlockId를_오름차순으로_조회한다() {
            // when
            SliceResponse<BlockedMemberResponse> response =
                    memberService.getBlockedMembers(null, 5, SortDirection.ASC);

            // then
            assertThat(response.content()).extracting("blockId").containsExactly(1L, 2L);
        }

        @Test
        void 차단한_멤버가_없으면_빈_리스트를_반환한다() {
            // given
            Member member = memberRepository.findById(3L).orElseThrow();
            given(memberUtil.getCurrentMember()).willReturn(member);

            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

            // when
            SliceResponse<BlockedMemberResponse> response =
                    memberService.getBlockedMembers(null, 5, SortDirection.DESC);

            // then
            Assertions.assertAll(
                    () -> assertThat(response.content().size()).isZero(),
                    () -> assertThat(response.isLast()).isTrue());
        }

        @Test
        void 마지막_페이지인_경우_isLast를_true로_반환한다() {
            // when
            SliceResponse<BlockedMemberResponse> response =
                    memberService.getBlockedMembers(null, 5, SortDirection.DESC);

            // then
            Assertions.assertAll(
                    () -> assertThat(response.content().size()).isEqualTo(2),
                    () -> assertThat(response.isLast()).isTrue());
        }

        @Test
        void 마지막_페이지가_아닌_경우_isLast를_false로_반환한다() {
            // when
            SliceResponse<BlockedMemberResponse> response =
                    memberService.getBlockedMembers(null, 1, SortDirection.DESC);

            // then
            Assertions.assertAll(
                    () -> assertThat(response.content().size()).isEqualTo(1),
                    () -> assertThat(response.isLast()).isFalse());
        }
    }
}
