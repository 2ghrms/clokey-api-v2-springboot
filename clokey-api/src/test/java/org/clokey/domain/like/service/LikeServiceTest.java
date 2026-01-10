package org.clokey.domain.like.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.util.List;
import org.clokey.IntegrationTest;
import org.clokey.TransactionUtil;
import org.clokey.domain.history.repository.HistoryImageRepository;
import org.clokey.domain.history.repository.HistoryRepository;
import org.clokey.domain.history.repository.SituationRepository;
import org.clokey.domain.like.dto.response.LikedHistoriesResponse;
import org.clokey.domain.like.dto.response.LikedMembersResponse;
import org.clokey.domain.like.repository.MemberLikeRepository;
import org.clokey.domain.member.repository.BlockRepository;
import org.clokey.domain.member.repository.FollowRepository;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.global.util.MemberUtil;
import org.clokey.history.entity.History;
import org.clokey.history.entity.HistoryImage;
import org.clokey.history.entity.Situation;
import org.clokey.like.entity.MemberLike;
import org.clokey.member.entity.Block;
import org.clokey.member.entity.Follow;
import org.clokey.member.entity.Member;
import org.clokey.member.entity.OauthInfo;
import org.clokey.member.enums.OauthProvider;
import org.clokey.response.SliceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class LikeServiceTest extends IntegrationTest {

    @Autowired private LikeService likeService;
    @Autowired private MemberLikeRepository memberLikeRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private SituationRepository situationRepository;
    @Autowired private HistoryRepository historyRepository;
    @Autowired private HistoryImageRepository historyImageRepository;
    @Autowired private FollowRepository followRepository;
    @Autowired private BlockRepository blockRepository;

    @Autowired private TransactionUtil transactionUtil;
    @MockitoBean private MemberUtil memberUtil;

    @Nested
    class 기록에_좋아요를_할_때 {
        @BeforeEach
        void setUp() {
            Member member1 =
                    Member.createMember(
                            "testEmail1",
                            "testClokeyId1",
                            "testNickName1",
                            OauthInfo.createOauthInfo("testOauthId1", OauthProvider.KAKAO));

            Member member2 =
                    Member.createMember(
                            "testEmail2",
                            "testClokeyId2",
                            "testNickName2",
                            OauthInfo.createOauthInfo("testOauthId2", OauthProvider.KAKAO));

            memberRepository.saveAll(List.of(member1, member2));
            given(memberUtil.getCurrentMember()).willReturn(member1);

            Situation situation1 = Situation.createSituation("testSituation1");
            situationRepository.save(situation1);

            History history1 =
                    History.createHistory(
                            LocalDate.of(2024, 12, 25),
                            "content1",
                            memberRepository.findById(2L).orElseThrow(),
                            situationRepository.findById(1L).orElseThrow());
            historyRepository.save(history1);
        }

        @Test
        void 좋아요를_누르면_좋아요를_추가한다() {
            // when
            likeService.toggleLike(1L);

            // then
            assertThat(memberLikeRepository.findByMemberIdAndHistoryId(1L, 1L).isPresent())
                    .isTrue();
        }

        @Test
        void 기록에_이미_좋아요가_있으면_좋아요를_취소한다() {
            memberLikeRepository.save(
                    MemberLike.createMemberLike(
                            memberRepository.findById(1L).orElseThrow(),
                            historyRepository.findById(1L).orElseThrow()));

            // when
            likeService.toggleLike(1L);

            // then
            assertThat(memberLikeRepository.findByMemberIdAndHistoryId(1L, 1L).isPresent())
                    .isFalse();
        }

        @Test
        void 차단된_회원이면_좋아요를_추가하지_않는다() {
            // given
            blockRepository.save(
                    Block.createBlock(
                            memberRepository.findById(2L).orElseThrow(),
                            memberRepository.findById(1L).orElseThrow()));

            // when
            likeService.toggleLike(1L);

            // then
            assertThat(memberLikeRepository.findByMemberIdAndHistoryId(1L, 1L).isPresent())
                    .isFalse();
        }
    }

    @Nested
    class 좋아요한_기록을_조회할_때 {

        @BeforeEach
        void setUp() {
            Member member1 =
                    Member.createMember(
                            "testEmail1",
                            "testClokeyId1",
                            "testNickName1",
                            OauthInfo.createOauthInfo("testOauthId1", OauthProvider.KAKAO));

            memberRepository.saveAll(List.of(member1));
            given(memberUtil.getCurrentMember()).willReturn(member1);

            Situation situation1 = Situation.createSituation("testSituation1");
            situationRepository.save(situation1);

            History history1 =
                    History.createHistory(
                            LocalDate.of(2024, 12, 25),
                            "content1",
                            memberRepository.findById(1L).orElseThrow(),
                            situationRepository.findById(1L).orElseThrow());
            History history2 =
                    History.createHistory(
                            LocalDate.of(2024, 12, 26),
                            "content2",
                            memberRepository.findById(1L).orElseThrow(),
                            situationRepository.findById(1L).orElseThrow());
            historyRepository.saveAll(List.of(history1, history2));

            HistoryImage historyImage1 =
                    HistoryImage.createHistoryImage("http://image1.url", history1);
            HistoryImage historyImage2 =
                    HistoryImage.createHistoryImage("http://image2.url", history2);
            historyImageRepository.saveAll(List.of(historyImage1, historyImage2));
        }

        @Test
        void 좋아요한_기록이_있으면_기록을_반환한다() {
            // given
            memberLikeRepository.saveAll(
                    List.of(
                            MemberLike.createMemberLike(
                                    memberUtil.getCurrentMember(),
                                    historyRepository.findById(1L).orElseThrow()),
                            MemberLike.createMemberLike(
                                    memberUtil.getCurrentMember(),
                                    historyRepository.findById(2L).orElseThrow())));

            // when
            SliceResponse<LikedHistoriesResponse.LikedHistoryPreview> response =
                    likeService.getLikedHistories(null, 10);

            // then
            assertThat(response.content()).hasSize(2);
            assertThat(response.isLast()).isTrue();

            assertThat(response.content())
                    .extracting("id", "imageUrl")
                    .containsExactly(
                            tuple(2L, "http://image2.url"), tuple(1L, "http://image1.url"));
        }

        @Test
        void 좋아요한_기록이_없으면_빈_리스트를_반환한다() {
            // when
            SliceResponse<LikedHistoriesResponse.LikedHistoryPreview> response =
                    likeService.getLikedHistories(null, 10);
            // then
            assertThat(response.content()).isEmpty();
            assertThat(response.isLast()).isTrue();
        }
    }

    @Nested
    class 좋아요한_유저를_조회할_때 {

        @BeforeEach
        void setUp() {
            Member member1 =
                    Member.createMember(
                            "testEmail1",
                            "testClokeyId1",
                            "testNickName1",
                            OauthInfo.createOauthInfo("testOauthId1", OauthProvider.KAKAO));

            Member member2 =
                    Member.createMember(
                            "testEmail2",
                            "testClokeyId2",
                            "testNickName2",
                            OauthInfo.createOauthInfo("testOauthId2", OauthProvider.KAKAO));

            Member member3 =
                    Member.createMember(
                            "testEmail3",
                            "testClokeyId3",
                            "testNickName3",
                            OauthInfo.createOauthInfo("testOauthId3", OauthProvider.KAKAO));
            memberRepository.saveAll(List.of(member1, member2, member3));
            given(memberUtil.getCurrentMember()).willReturn(member1);

            Situation situation1 = Situation.createSituation("testSituation1");
            situationRepository.save(situation1);

            History history1 =
                    History.createHistory(
                            LocalDate.of(2024, 12, 25),
                            "content1",
                            memberRepository.findById(1L).orElseThrow(),
                            situationRepository.findById(1L).orElseThrow());
            historyRepository.save(history1);
        }

        @Test
        void 좋아요한_유저가_있으면_유저를_반환한다() {
            // given
            memberLikeRepository.saveAll(
                    List.of(
                            MemberLike.createMemberLike(
                                    memberRepository.findById(2L).orElseThrow(),
                                    historyRepository.findById(1L).orElseThrow()),
                            MemberLike.createMemberLike(
                                    memberRepository.findById(3L).orElseThrow(),
                                    historyRepository.findById(1L).orElseThrow())));

            followRepository.save(
                    Follow.createFollow(
                            memberRepository.findById(1L).orElseThrow(),
                            memberRepository.findById(2L).orElseThrow()));

            // when
            SliceResponse<LikedMembersResponse.LikedMemberPreview> response =
                    likeService.getLikedMembers(1L, null, 10);

            // then
            assertThat(response.content()).hasSize(2);
            assertThat(response.isLast()).isTrue();

            assertThat(response.content())
                    .extracting("id", "codiveId", "imageUrl", "nickname", "followStatus")
                    .containsExactly(
                            tuple(3L, "testClokeyId3", null, "testNickName3", false),
                            tuple(2L, "testClokeyId2", null, "testNickName2", true));
        }

        @Test
        void 좋아요한_기록이_없으면_빈_리스트를_반환한다() {
            // when
            SliceResponse<LikedMembersResponse.LikedMemberPreview> response =
                    likeService.getLikedMembers(1L, null, 10);

            // then
            assertThat(response.content()).isEmpty();
            assertThat(response.isLast()).isTrue();
        }
    }
}
