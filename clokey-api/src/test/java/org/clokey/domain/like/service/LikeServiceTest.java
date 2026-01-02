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
import org.clokey.domain.like.repository.MemberLikeRepository;
import org.clokey.domain.member.repository.BlockRepository;
import org.clokey.domain.member.repository.FollowRepository;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.global.util.MemberUtil;
import org.clokey.history.entity.History;
import org.clokey.history.entity.Situation;
import org.clokey.like.entity.MemberLike;
import org.clokey.member.entity.Block;
import org.clokey.member.entity.Member;
import org.clokey.member.entity.OauthInfo;
import org.clokey.member.enums.OauthProvider;
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
}
