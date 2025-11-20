package org.clokey.domain.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;
import org.clokey.IntegrationTest;
import org.clokey.TransactionUtil;
import org.clokey.domain.member.dto.request.DuplicatedIdCheckRequest;
import org.clokey.domain.member.dto.request.ProfileUpdateRequest;
import org.clokey.domain.member.dto.response.BlockedMemberResponse;
import org.clokey.domain.member.dto.response.FollowMemberResponse;
import org.clokey.domain.member.dto.response.MemberInfoResponse;
import org.clokey.domain.member.dto.response.MyselfCheckResponse;
import org.clokey.domain.member.exception.MemberErrorCode;
import org.clokey.domain.member.repository.BlockRepository;
import org.clokey.domain.member.repository.FollowRepository;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.domain.member.repository.PendingFollowRepository;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.paging.SortDirection;
import org.clokey.global.util.MemberUtil;
import org.clokey.member.entity.Block;
import org.clokey.member.entity.Follow;
import org.clokey.member.entity.Member;
import org.clokey.member.entity.OauthInfo;
import org.clokey.member.entity.PendingFollow;
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
    @Autowired private FollowRepository followRepository;
    @Autowired private PendingFollowRepository pendingFollowRepository;
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
    class 공개계정_팔로우_언팔로우_할_때 {
        @BeforeEach
        void setUp() {
            Member me =
                    Member.createMember(
                            "me@test.com",
                            "meId",
                            "me",
                            OauthInfo.createOauthInfo("meOauth", OauthProvider.KAKAO));
            Member publicUser =
                    Member.createMember(
                            "public@test.com",
                            "publicId",
                            "pub",
                            OauthInfo.createOauthInfo("pubOauth", OauthProvider.KAKAO));
            Member privateUser =
                    Member.createMember(
                            "private@test.com",
                            "privateId",
                            "pri",
                            OauthInfo.createOauthInfo("priOauth", OauthProvider.KAKAO));
            privateUser.changeVisibility();

            memberRepository.saveAll(List.of(me, publicUser, privateUser));
            given(memberUtil.getCurrentMember()).willReturn(me);
        }

        @Test
        void 공개계정을_팔로우하면_팔로우를_추가한다() {
            // when
            memberService.toggleFollow(2L);

            // then
            assertThat(followRepository.existsByFollowFrom_IdAndFollowTo_Id(1L, 2L)).isTrue();
        }

        @Test
        void 공개계정을_이미팔로우중이면_취소한다() {
            // given
            followRepository.save(
                    Follow.createFollow(
                            memberRepository.findById(1L).orElseThrow(),
                            memberRepository.findById(2L).orElseThrow()));

            // when
            memberService.toggleFollow(2L);

            // then
            assertThat(followRepository.existsByFollowFrom_IdAndFollowTo_Id(1L, 2L)).isFalse();
        }

        @Test
        void 자기자신을_팔로우하면_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> memberService.toggleFollow(1L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(MemberErrorCode.CANNOT_FOLLOW_MYSELF.getMessage());
        }

        @Test
        void 차단한_사용자를_팔로우하면_예외가_발생한다() {
            // given
            blockRepository.save(
                    Block.createBlock(
                            memberRepository.findById(1L).orElseThrow(),
                            memberRepository.findById(2L).orElseThrow()));

            // when & then
            assertThatThrownBy(() -> memberService.toggleFollow(2L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(MemberErrorCode.CANNOT_FOLLOW_BLOCKED.getMessage());
        }

        @Test
        void 차단된_사용자가_팔로우하면_예외가_발생한다() {
            // given
            blockRepository.save(
                    Block.createBlock(
                            memberRepository.findById(2L).orElseThrow(),
                            memberRepository.findById(1L).orElseThrow()));

            // when & then
            assertThatThrownBy(() -> memberService.toggleFollow(2L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(MemberErrorCode.CANNOT_FOLLOW_BLOCKED.getMessage());
        }

        @Test
        void 비공개계정을_팔로우하면_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> memberService.toggleFollow(3L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(MemberErrorCode.MUST_REQUEST_FOLLOW.getMessage());
        }
    }

    @Nested
    class 비공개계정_팔로우_언팔로우_할_때 {

        @BeforeEach
        void setUp() {
            Member me =
                    Member.createMember(
                            "me@test.com",
                            "meId",
                            "me",
                            OauthInfo.createOauthInfo("meOauth", OauthProvider.KAKAO));
            Member publicUser =
                    Member.createMember(
                            "public@test.com",
                            "publicId",
                            "pub",
                            OauthInfo.createOauthInfo("pubOauth", OauthProvider.KAKAO));
            Member privateUser =
                    Member.createMember(
                            "private@test.com",
                            "privateId",
                            "pri",
                            OauthInfo.createOauthInfo("priOauth", OauthProvider.KAKAO));
            privateUser.changeVisibility();

            memberRepository.saveAll(List.of(me, publicUser, privateUser));
            given(memberUtil.getCurrentMember()).willReturn(me);
        }

        @Test
        void 비공개계정을_팔로우하면_팔로우요청을_추가한다() {
            // when
            memberService.togglePendingFollow(3L);

            // then
            Optional<PendingFollow> pendingFollow =
                    pendingFollowRepository.findByFollowFrom_IdAndFollowTo_Id(1L, 3L);

            assertThat(pendingFollow).isPresent(); // 존재 여부 확인
        }

        @Test
        void 비공개계정을_이미요청중이면_취소한다() {
            // given
            pendingFollowRepository.save(
                    PendingFollow.createPendingFollow(
                            memberRepository.findById(1L).orElseThrow(),
                            memberRepository.findById(3L).orElseThrow()));

            // when
            memberService.togglePendingFollow(3L);

            // then
            Optional<PendingFollow> pendingFollow =
                    pendingFollowRepository.findByFollowFrom_IdAndFollowTo_Id(1L, 3L);

            assertThat(pendingFollow).isEmpty(); // 존재 여부 확인
        }

        @Test
        void 비공개계정을_이미팔로우중이면_취소한다() {
            // given
            followRepository.save(
                    Follow.createFollow(
                            memberRepository.findById(1L).orElseThrow(),
                            memberRepository.findById(3L).orElseThrow()));

            // when
            memberService.togglePendingFollow(3L);

            // then
            assertThat(followRepository.existsByFollowFrom_IdAndFollowTo_Id(1L, 3L)).isFalse();
        }

        @Test
        void 자기자신을_팔로우하면_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> memberService.togglePendingFollow(1L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(MemberErrorCode.CANNOT_FOLLOW_MYSELF.getMessage());
        }

        @Test
        void 차단한_사용자를_팔로우요청하면_예외가_발생한다() {
            // given
            blockRepository.save(
                    Block.createBlock(
                            memberRepository.findById(1L).orElseThrow(),
                            memberRepository.findById(3L).orElseThrow()));

            // when & then
            assertThatThrownBy(() -> memberService.togglePendingFollow(3L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(MemberErrorCode.CANNOT_FOLLOW_BLOCKED.getMessage());
        }

        @Test
        void 차단된_사용자가_팔로우요청하면_예외가_발생한다() {
            // given
            blockRepository.save(
                    Block.createBlock(
                            memberRepository.findById(3L).orElseThrow(),
                            memberRepository.findById(1L).orElseThrow()));

            // when & then
            assertThatThrownBy(() -> memberService.togglePendingFollow(3L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(MemberErrorCode.CANNOT_FOLLOW_BLOCKED.getMessage());
        }

        @Test
        void 공개계정을_팔로우요청하면_예외가_발생한다() {
            // given
            memberRepository.findById(2L).orElseThrow().changeVisibility();

            // when & then
            assertThatThrownBy(() -> memberService.togglePendingFollow(2L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(MemberErrorCode.MUST_FOLLOW.getMessage());
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

    @Nested
    class 팔로잉_팔로우_목록을_조회할_때 {

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
            Member member4 =
                    Member.createMember(
                            "testEmail4",
                            "testCodiveId4",
                            "testNickName4",
                            OauthInfo.createOauthInfo("testOauthId4", OauthProvider.KAKAO));

            member1.changeVisibility();
            member4.changeVisibility();
            memberRepository.saveAll(List.of(member1, member2, member3, member4));
            given(memberUtil.getCurrentMember()).willReturn(member1);

            Follow follow12 = Follow.createFollow(member1, member2);
            Follow follow13 = Follow.createFollow(member1, member3);
            Follow follow23 = Follow.createFollow(member2, member3);
            Follow follow31 = Follow.createFollow(member3, member1);
            Follow follow32 = Follow.createFollow(member3, member2);
            Follow follow42 = Follow.createFollow(member4, member2);
            Block block = Block.createBlock(member3, member1);

            followRepository.saveAll(
                    List.of(follow12, follow13, follow23, follow31, follow32, follow42));
            blockRepository.save(block);
        }

        @Test
        void 유효한_요청이면_팔로잉_목록을_반환한다() {
            // when
            SliceResponse<FollowMemberResponse> response =
                    memberService.getFollows(1L, null, true, 10);
            // then
            assertThat(response.content())
                    .extracting("codiveId", "isMe")
                    .containsExactly(tuple("testCodiveId2", false));
        }

        @Test
        void 유효한_요청이면_팔로워_목록을_반환한다() {
            // when
            SliceResponse<FollowMemberResponse> response =
                    memberService.getFollows(2L, null, false, 10);
            // then
            assertThat(response.content())
                    .extracting("codiveId")
                    .containsExactly("testCodiveId4", "testCodiveId1");
        }

        @Test
        void 차단_관계의_멤버는_목록에_표시하지_않는다() {
            // when
            SliceResponse<FollowMemberResponse> response =
                    memberService.getFollows(2L, null, true, 10);

            // then
            assertThat(response.content()).isEmpty();
        }

        @Test
        void 조회된_멤버가_요청자일_경우_isMe에_true를_반환한다() {
            // when
            SliceResponse<FollowMemberResponse> response =
                    memberService.getFollows(2L, null, false, 10);

            // then
            assertThat(response.content())
                    .extracting("codiveId", "isMe")
                    .containsExactly(tuple("testCodiveId4", false), tuple("testCodiveId1", true));
        }

        @Test
        void 비공개_계정의_팔로잉_또는_팔로우_목록을_요청할_경우_예외가_발생한다() {
            // then
            assertThatThrownBy(() -> memberService.getFollows(4L, null, true, 10))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(MemberErrorCode.PRIVATE_MEMBER_ACCESS_DENIED.getMessage());
        }

        @Test
        void 차단_당한_계정의_팔로잉_또는_팔로우_목록을_요청할_경우_예외가_발생한다() {
            // then
            assertThatThrownBy(() -> memberService.getFollows(3L, null, true, 10))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(MemberErrorCode.BLOCKED_MEMBER_ACCESS_DENIED.getMessage());
        }

        @Test
        void 마지막_페이지가_아닌_경우_isLast를_false로_반환한다() {
            // when
            SliceResponse<FollowMemberResponse> response =
                    memberService.getFollows(2L, null, false, 1);

            // then
            Assertions.assertAll(
                    () -> assertThat(response.content().size()).isEqualTo(1),
                    () -> assertThat(response.isLast()).isFalse());
        }
    }

    @Nested
    class 회원_정보를_조회할_때 {

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
            memberRepository.saveAll(List.of(member1, member2));
            given(memberUtil.getCurrentMember()).willReturn(member1);
            Follow follow21 = Follow.createFollow(member2, member1);
            followRepository.save(follow21);
        }

        @Test
        void 유효한_요청이면_회원_정보를_반환한다() {
            // when
            MemberInfoResponse response = memberService.getMemberInfo(2L);
            // then
            Assertions.assertAll(
                    () -> assertThat(response.codiveId()).isEqualTo("testCodiveId2"),
                    () -> assertThat(response.nickname()).isEqualTo("testNickName2"),
                    () -> assertThat(response.followerCount()).isZero(),
                    () -> assertThat(response.isMe()).isFalse());
        }

        @Test
        void 본인_정보_요청_시_isMe를_true로_반환한다() {
            // when
            MemberInfoResponse response = memberService.getMemberInfo(1L);
            // then
            Assertions.assertAll(
                    () -> assertThat(response.codiveId()).isEqualTo("testCodiveId1"),
                    () -> assertThat(response.nickname()).isEqualTo("testNickName1"),
                    () -> assertThat(response.followerCount()).isOne(),
                    () -> assertThat(response.isMe()).isTrue());
        }

        @Test
        void 존재하지_않는_memberId로_요청한_경우_예외가_발생한다() {
            //  when & then
            assertThatThrownBy(() -> memberService.getMemberInfo(33L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
        }
    }
}
