package org.clokey.domain.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.clokey.IntegrationTest;
import org.clokey.domain.feed.dto.response.FeedListResponse;
import org.clokey.domain.feed.query.FeedCursor;
import org.clokey.domain.feed.query.FollowScope;
import org.clokey.domain.feed.util.FeedCursorUtil;
import org.clokey.domain.history.exception.SituationErrorCode;
import org.clokey.domain.history.exception.StyleErrorCode;
import org.clokey.domain.history.repository.HistoryImageRepository;
import org.clokey.domain.history.repository.HistoryRepository;
import org.clokey.domain.history.repository.SituationRepository;
import org.clokey.domain.like.repository.MemberLikeRepository;
import org.clokey.domain.member.repository.BlockRepository;
import org.clokey.domain.member.repository.FollowRepository;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.exception.BaseCustomException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class FeedServiceTest extends IntegrationTest {

    @Autowired private FeedService feedService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private SituationRepository situationRepository;
    @Autowired private HistoryRepository historyRepository;
    @Autowired private HistoryImageRepository historyImageRepository;
    @Autowired private MemberLikeRepository memberLikeRepository;
    @Autowired private BlockRepository blockRepository;
    @Autowired private FollowRepository followRepository;

    @MockitoBean private MemberUtil memberUtil;

    @Nested
    class 전체_피드_조회할_때 {

        @BeforeEach
        void setUp() {
            Member member1 =
                    Member.createMember(
                            "testEmail10",
                            "testNickName10",
                            OauthInfo.createOauthInfo("oauth-current-10", OauthProvider.KAKAO));
            Member member2 =
                    Member.createMember(
                            "testEmail20",
                            "testNickName20",
                            OauthInfo.createOauthInfo("oauth-a-20", OauthProvider.KAKAO));
            Member member3 =
                    Member.createMember(
                            "testEmail30",
                            "testNickName30",
                            OauthInfo.createOauthInfo("oauth-b-30", OauthProvider.KAKAO));
            memberRepository.saveAll(List.of(member1, member2, member3));
            given(memberUtil.getCurrentMember()).willReturn(member1);

            Situation situation = Situation.createSituation("daily");
            situationRepository.save(situation);

            History history2_1 =
                    History.createHistory(LocalDate.of(2025, 1, 1), "A1", member2, situation);
            History history2_2 =
                    History.createHistory(LocalDate.of(2025, 1, 2), "A2", member2, situation);
            History history2_3 =
                    History.createHistory(LocalDate.of(2025, 1, 3), "A3", member2, situation);
            historyRepository.saveAll(List.of(history2_1, history2_2, history2_3));

            History history3_1 =
                    History.createHistory(LocalDate.of(2025, 1, 4), "B1", member3, situation);
            History history3_2 =
                    History.createHistory(LocalDate.of(2025, 1, 5), "B2", member3, situation);
            historyRepository.saveAll(List.of(history3_1, history3_2));

            historyImageRepository.saveAll(
                    List.of(
                            HistoryImage.createHistoryImage("img-a1", history2_1),
                            HistoryImage.createHistoryImage("img-a2", history2_2),
                            HistoryImage.createHistoryImage("img-a3", history2_3),
                            HistoryImage.createHistoryImage("img-b1", history3_1),
                            HistoryImage.createHistoryImage("img-b2", history3_2)));
        }

        @Test
        void 전체_피드를_조회하면_작성자별로_분산되고_스킵된_피드가_커서에_포함된다() {
            // when
            FeedListResponse response =
                    feedService.getFeeds(FollowScope.ALL, List.of(), List.of(), 3, null);

            Map<String, Long> historyIds =
                    historyRepository.findAll().stream()
                            .collect(
                                    Collectors.toMap(
                                            History::getContent,
                                            History::getId,
                                            (left, right) -> left));
            Long history2_1 = historyIds.get("A1");
            Long history2_2 = historyIds.get("A2");
            Long history2_3 = historyIds.get("A3");
            Long history3_1 = historyIds.get("B1");
            Long history3_2 = historyIds.get("B2");
            // then
            assertThat(response.items()).hasSize(3);
            List<Long> feedIds =
                    response.items().stream()
                            .map(FeedListResponse.FeedItemResponse::feedId)
                            .toList();

            assertThat(feedIds).containsExactly(history3_2, history2_3, history3_1);
            assertThat(response.hasNext()).isTrue();

            FeedCursor cursor = FeedCursorUtil.decode(response.nextCursor());
            assertThat(cursor.feedId()).isEqualTo(history2_1);
            assertThat(cursor.pendingFeedIds()).containsExactly(history2_2, history2_1);
        }

        @Test
        void 다음_페이지에서_스킵된_피드를_먼저_반환한다() {
            // when
            FeedListResponse first =
                    feedService.getFeeds(FollowScope.ALL, List.of(), List.of(), 3, null);

            FeedListResponse second =
                    feedService.getFeeds(
                            FollowScope.ALL, List.of(), List.of(), 3, first.nextCursor());

            Map<String, Long> historyIds =
                    historyRepository.findAll().stream()
                            .collect(
                                    Collectors.toMap(
                                            History::getContent,
                                            History::getId,
                                            (left, right) -> left));
            Long history2_1 = historyIds.get("A1");
            Long history2_2 = historyIds.get("A2");
            // then
            assertThat(second.items()).hasSize(2);
            assertThat(second.items())
                    .extracting(FeedListResponse.FeedItemResponse::feedId)
                    .containsExactly(history2_2, history2_1);
            assertThat(second.hasNext()).isFalse();
            assertThat(second.nextCursor()).isNull();
        }

        @Test
        void 존재하지_않는_스타일_ID면_예외가_발생한다() {
            // given
            assertThatThrownBy(
                            () ->
                                    feedService.getFeeds(
                                            FollowScope.ALL, List.of(999L), List.of(), 3, null))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(StyleErrorCode.STYLE_NOT_FOUND.getMessage());
        }

        @Test
        void 존재하지_않는_상황_ID면_예외가_발생한다() {
            // given
            assertThatThrownBy(
                            () ->
                                    feedService.getFeeds(
                                            FollowScope.ALL, List.of(), List.of(999L), 3, null))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(SituationErrorCode.SITUATION_NOT_FOUND.getMessage());
        }

        @Test
        void 차단한_사용자의_피드는_노출되지_않는다() {
            // given
            Member member1 = memberRepository.findByNickname("testNickName10").orElseThrow();
            Member member2 = memberRepository.findByNickname("testNickName20").orElseThrow();
            blockRepository.save(Block.createBlock(member1, member2));
            // when
            FeedListResponse response =
                    feedService.getFeeds(FollowScope.ALL, List.of(), List.of(), 3, null);

            Map<String, Long> historyIds =
                    historyRepository.findAll().stream()
                            .collect(
                                    Collectors.toMap(
                                            History::getContent,
                                            History::getId,
                                            (left, right) -> left));
            Long history2_1 = historyIds.get("A1");
            Long history2_2 = historyIds.get("A2");
            Long history2_3 = historyIds.get("A3");

            List<Long> feedIds =
                    response.items().stream()
                            .map(FeedListResponse.FeedItemResponse::feedId)
                            .toList();
            // then
            assertThat(feedIds).isNotEmpty();
            assertThat(feedIds).doesNotContain(history2_1, history2_2, history2_3);
            assertThat(response.items()).hasSize(2);
            assertThat(response.hasNext()).isFalse();
        }
    }

    @Nested
    class 팔로잉_피드_조회할_때 {

        @BeforeEach
        void setUp() {
            Member member1 =
                    Member.createMember(
                            "testEmail1",
                            "testNickName1",
                            OauthInfo.createOauthInfo("oauth-current", OauthProvider.KAKAO));
            Member member2 =
                    Member.createMember(
                            "testEmail2",
                            "testNickName2",
                            OauthInfo.createOauthInfo("oauth-a", OauthProvider.KAKAO));
            Member member3 =
                    Member.createMember(
                            "testEmail3",
                            "testNickName3",
                            OauthInfo.createOauthInfo("oauth-b", OauthProvider.KAKAO));
            memberRepository.saveAll(List.of(member1, member2, member3));
            given(memberUtil.getCurrentMember()).willReturn(member1);

            Situation situation = Situation.createSituation("daily");
            situationRepository.save(situation);

            History history2_1 =
                    History.createHistory(LocalDate.of(2025, 1, 1), "A1", member2, situation);
            History history2_2 =
                    History.createHistory(LocalDate.of(2025, 1, 2), "A2", member2, situation);
            History history2_3 =
                    History.createHistory(LocalDate.of(2025, 1, 3), "A3", member2, situation);
            historyRepository.saveAll(List.of(history2_1, history2_2, history2_3));

            History history3_1 =
                    History.createHistory(LocalDate.of(2025, 1, 4), "B1", member3, situation);
            History history3_2 =
                    History.createHistory(LocalDate.of(2025, 1, 5), "B2", member3, situation);
            historyRepository.saveAll(List.of(history3_1, history3_2));

            historyImageRepository.saveAll(
                    List.of(
                            HistoryImage.createHistoryImage("img-a1", history2_1),
                            HistoryImage.createHistoryImage("img-a2", history2_2),
                            HistoryImage.createHistoryImage("img-a3", history2_3),
                            HistoryImage.createHistoryImage("img-b1", history3_1),
                            HistoryImage.createHistoryImage("img-b2", history3_2)));

            followRepository.saveAll(
                    List.of(
                            Follow.createFollow(member1, member2),
                            Follow.createFollow(member1, member3)));

            memberLikeRepository.save(MemberLike.createMemberLike(member1, history3_2));
        }

        @Test
        void 팔로잉_피드를_조회하면_최신순으로_반환된다() {
            // given
            FeedListResponse response =
                    feedService.getFeeds(FollowScope.FOLLOWING, List.of(), List.of(), 3, null);

            Map<String, Long> historyIds =
                    historyRepository.findAll().stream()
                            .collect(
                                    Collectors.toMap(
                                            History::getContent,
                                            History::getId,
                                            (left, right) -> left));
            Long history2_3 = historyIds.get("A3");
            Long history3_1 = historyIds.get("B1");
            Long history3_2 = historyIds.get("B2");

            // then
            assertThat(response.items()).hasSize(3);
            List<Long> feedIds =
                    response.items().stream()
                            .map(FeedListResponse.FeedItemResponse::feedId)
                            .toList();

            assertThat(feedIds).containsExactly(history3_2, history3_1, history2_3);
            assertThat(response.hasNext()).isTrue();

            FeedCursor cursor = FeedCursorUtil.decode(response.nextCursor());
            assertThat(cursor.pendingFeedIds()).isEmpty();
        }

        @Test
        void 다음_페이지에서_나머지_피드를_조회한다() {
            // when
            FeedListResponse first =
                    feedService.getFeeds(FollowScope.FOLLOWING, List.of(), List.of(), 3, null);

            FeedListResponse second =
                    feedService.getFeeds(
                            FollowScope.FOLLOWING, List.of(), List.of(), 3, first.nextCursor());

            Map<String, Long> historyIds =
                    historyRepository.findAll().stream()
                            .collect(
                                    Collectors.toMap(
                                            History::getContent,
                                            History::getId,
                                            (left, right) -> left));
            Long history2_1 = historyIds.get("A1");
            Long history2_2 = historyIds.get("A2");
            // then
            assertThat(second.items()).hasSize(2);
            assertThat(second.items())
                    .extracting(FeedListResponse.FeedItemResponse::feedId)
                    .containsExactly(history2_2, history2_1);
            assertThat(second.hasNext()).isFalse();
            assertThat(second.nextCursor()).isNull();
        }
    }
}
