package org.clokey.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;
import org.clokey.IntegrationTest;
import org.clokey.RedisCleaner;
import org.clokey.auth.entity.RefreshToken;
import org.clokey.category.entity.Category;
import org.clokey.cloth.entity.Cloth;
import org.clokey.cloth.enums.Season;
import org.clokey.coordinate.entity.Coordinate;
import org.clokey.coordinate.entity.CoordinateCloth;
import org.clokey.domain.auth.dto.AccessTokenDto;
import org.clokey.domain.auth.dto.RefreshTokenDto;
import org.clokey.domain.auth.dto.request.DeviceTokenRenewRequest;
import org.clokey.domain.auth.dto.request.TokenReissueRequest;
import org.clokey.domain.auth.dto.request.UserStatusUpdateRequest;
import org.clokey.domain.auth.dto.response.TokenResponse;
import org.clokey.domain.auth.dto.response.UserStatusResponse;
import org.clokey.domain.auth.enums.RegisterStatus;
import org.clokey.domain.auth.exception.AuthErrorCode;
import org.clokey.domain.auth.repository.RefreshTokenRepository;
import org.clokey.domain.category.repository.CategoryRepository;
import org.clokey.domain.cloth.repository.ClothRepository;
import org.clokey.domain.comment.repository.CommentRepository;
import org.clokey.domain.coordinate.repository.CoordinateClothRepository;
import org.clokey.domain.coordinate.repository.CoordinateRepository;
import org.clokey.domain.history.repository.HashtagRepository;
import org.clokey.domain.history.repository.HistoryClothTagRepository;
import org.clokey.domain.history.repository.HistoryHashtagRepository;
import org.clokey.domain.history.repository.HistoryImageRepository;
import org.clokey.domain.history.repository.HistoryRepository;
import org.clokey.domain.history.repository.HistoryStyleRepository;
import org.clokey.domain.history.repository.SituationRepository;
import org.clokey.domain.history.repository.StyleRepository;
import org.clokey.domain.image.event.ImagesDeleteEvent;
import org.clokey.domain.like.repository.MemberLikeRepository;
import org.clokey.domain.lookbook.repository.LookBookRepository;
import org.clokey.domain.member.repository.BlockRepository;
import org.clokey.domain.member.repository.FollowRepository;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.domain.member.repository.PendingFollowRepository;
import org.clokey.domain.notification.repository.CodiveNotificationRepository;
import org.clokey.domain.report.repository.ReportRepository;
import org.clokey.domain.term.repository.MemberTermRepository;
import org.clokey.domain.term.repository.TermRepository;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.util.MemberUtil;
import org.clokey.history.entity.Hashtag;
import org.clokey.history.entity.History;
import org.clokey.history.entity.HistoryClothTag;
import org.clokey.history.entity.HistoryHashtag;
import org.clokey.history.entity.HistoryImage;
import org.clokey.history.entity.HistoryStyle;
import org.clokey.history.entity.Situation;
import org.clokey.history.entity.Style;
import org.clokey.like.entity.MemberLike;
import org.clokey.lookbook.entity.LookBook;
import org.clokey.member.entity.Block;
import org.clokey.member.entity.Follow;
import org.clokey.member.entity.Member;
import org.clokey.member.entity.OauthInfo;
import org.clokey.member.entity.PendingFollow;
import org.clokey.member.enums.MemberRole;
import org.clokey.member.enums.MemberStatus;
import org.clokey.member.enums.OauthProvider;
import org.clokey.member.enums.Visibility;
import org.clokey.notification.entity.CodiveNotification;
import org.clokey.notification.enums.RedirectType;
import org.clokey.report.entity.Report;
import org.clokey.report.enums.ReportReason;
import org.clokey.report.enums.TargetType;
import org.clokey.term.entity.MemberTerm;
import org.clokey.term.entity.Term;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

@RecordApplicationEvents
class AuthServiceTest extends IntegrationTest {

    @Autowired private RedisCleaner redisCleaner;

    @Autowired private AuthService authService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private TermRepository termRepository;
    @Autowired private MemberTermRepository memberTermRepository;
    @Autowired private RefreshTokenRepository refreshTokenRepository;
    @Autowired private HistoryRepository historyRepository;
    @Autowired private HistoryImageRepository historyImageRepository;
    @Autowired private HistoryClothTagRepository historyClothTagRepository;
    @Autowired private HistoryHashtagRepository historyHashtagRepository;
    @Autowired private HistoryStyleRepository historyStyleRepository;
    @Autowired private MemberLikeRepository memberLikeRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private CoordinateRepository coordinateRepository;
    @Autowired private CoordinateClothRepository coordinateClothRepository;
    @Autowired private LookBookRepository lookBookRepository;
    @Autowired private ClothRepository clothRepository;
    @Autowired private CodiveNotificationRepository codiveNotificationRepository;
    @Autowired private FollowRepository followRepository;
    @Autowired private PendingFollowRepository pendingFollowRepository;
    @Autowired private BlockRepository blockRepository;
    @Autowired private ReportRepository reportRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private SituationRepository situationRepository;
    @Autowired private StyleRepository styleRepository;
    @Autowired private HashtagRepository hashtagRepository;

    @MockitoBean private JwtTokenService jwtTokenService;
    @MockitoBean private MemberUtil memberUtil;
    @Autowired private ApplicationEvents applicationEvents;

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

    @Nested
    class 회원_상태를_변경할_때 {

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
        @Transactional
        void 활성화_요청이면_회원을_ACTIVE_상태로_변경하고_inactiveDate를_null로_설정한다() {
            // given
            Member member = memberRepository.findById(1L).orElseThrow();
            member.deactivate();
            memberRepository.save(member);

            UserStatusUpdateRequest request = new UserStatusUpdateRequest(true);

            // when
            authService.updateUserStatus(request);

            // then
            Member updatedMember = memberRepository.findById(1L).orElseThrow();
            assertThat(updatedMember.getMemberStatus()).isEqualTo(MemberStatus.ACTIVE);
            assertThat(updatedMember.getInactiveDate()).isNull();
        }

        @Test
        @Transactional
        void 비활성화_요청이면_회원을_INACTIVE_상태로_변경하고_inactiveDate를_현재_날짜로_설정한다() {
            // given
            UserStatusUpdateRequest request = new UserStatusUpdateRequest(false);

            // when
            authService.updateUserStatus(request);

            // then
            Member updatedMember = memberRepository.findById(1L).orElseThrow();
            assertThat(updatedMember.getMemberStatus()).isEqualTo(MemberStatus.INACTIVE);
            assertThat(updatedMember.getInactiveDate()).isNotNull();
        }
    }

    @Nested
    class 회원_탈퇴할_때 {

        private Long targetMemberId;
        private Long otherMemberId;

        @BeforeEach
        void setUp() {
            // 회원 생성 (프로필 이미지 포함)
            Member targetMember =
                    Member.createMember(
                            "targetEmail",
                            "targetClokeyId",
                            "targetNickName",
                            OauthInfo.createOauthInfo("targetOauthId", OauthProvider.KAKAO));
            targetMember.updateProfile(
                    "targetNickName",
                    "targetClokeyId",
                    "profileImageUrl",
                    "profileBackImageUrl",
                    "한줄소개",
                    Visibility.PUBLIC);

            Member otherMember =
                    Member.createMember(
                            "otherEmail",
                            "otherClokeyId",
                            "otherNickName",
                            OauthInfo.createOauthInfo("otherOauthId", OauthProvider.KAKAO));

            List<Member> savedMembers =
                    memberRepository.saveAll(List.of(targetMember, otherMember));
            targetMember = savedMembers.get(0);
            otherMember = savedMembers.get(1);
            targetMemberId = targetMember.getId();
            otherMemberId = otherMember.getId();
            given(memberUtil.getCurrentMember()).willReturn(targetMember);

            // 마스터 데이터 생성
            Category category = Category.createCategory("testCategory", null);
            categoryRepository.save(category);

            Situation situation = Situation.createSituation("testSituation");
            situationRepository.save(situation);

            Style style1 = Style.createStyle("testStyle1");
            Style style2 = Style.createStyle("testStyle2");
            styleRepository.saveAll(List.of(style1, style2));

            Hashtag hashtag1 = Hashtag.createHashtag("testhashtag1");
            Hashtag hashtag2 = Hashtag.createHashtag("testhashtag2");
            hashtagRepository.saveAll(List.of(hashtag1, hashtag2));

            Term term = Term.createTerm("testTitle", "testBody", false);
            termRepository.save(term);

            // History 관련 데이터 생성
            History history =
                    History.createHistory(
                            LocalDate.of(2025, 1, 1), "testContent", targetMember, situation);
            historyRepository.save(history);

            HistoryImage historyImage1 =
                    HistoryImage.createHistoryImage("historyImageUrl1", history);
            HistoryImage historyImage2 =
                    HistoryImage.createHistoryImage("historyImageUrl2", history);
            historyImageRepository.saveAll(List.of(historyImage1, historyImage2));

            Cloth cloth1 =
                    Cloth.createCloth(
                            "clothImageUrl1",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            targetMember);
            Cloth cloth2 =
                    Cloth.createCloth(
                            "clothImageUrl2",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            targetMember);
            clothRepository.saveAll(List.of(cloth1, cloth2));

            HistoryClothTag historyClothTag1 =
                    HistoryClothTag.createHistoryClothTag(historyImage1, cloth1, 0.25, 0.5);
            HistoryClothTag historyClothTag2 =
                    HistoryClothTag.createHistoryClothTag(historyImage2, cloth2, 0.75, 0.33);
            historyClothTagRepository.bulkInsertHistoryClothTags(
                    List.of(historyClothTag1, historyClothTag2));

            HistoryStyle historyStyle1 = HistoryStyle.createHistoryStyle(history, style1);
            HistoryStyle historyStyle2 = HistoryStyle.createHistoryStyle(history, style2);
            historyStyleRepository.bulkInsertHistoryStyles(List.of(historyStyle1, historyStyle2));

            HistoryHashtag historyHashtag1 = HistoryHashtag.createHistoryHashtag(history, hashtag1);
            HistoryHashtag historyHashtag2 = HistoryHashtag.createHistoryHashtag(history, hashtag2);
            historyHashtagRepository.bulkInsertHistoryHashtags(
                    List.of(historyHashtag1, historyHashtag2));

            // MemberLike 생성
            MemberLike memberLike = MemberLike.createMemberLike(targetMember, history);
            memberLikeRepository.save(memberLike);

            // Comment 생성
            org.clokey.comment.entitiy.Comment comment =
                    org.clokey.comment.entitiy.Comment.createParentComment(
                            "testComment", targetMember, history);
            commentRepository.save(comment);

            // Coordinate 관련 데이터 생성
            LookBook lookBook = LookBook.createLookBook("testLookBook", targetMember);
            lookBookRepository.save(lookBook);

            Coordinate coordinate =
                    Coordinate.createCoordinateManual(
                            "testCoordinate",
                            "testMemo",
                            "coordinateImageUrl",
                            targetMember,
                            lookBook);
            coordinateRepository.save(coordinate);

            CoordinateCloth coordinateCloth =
                    CoordinateCloth.createCoordinateCloth(
                            1.0, 1.0, 1.0, 30.0, 1, coordinate, cloth1);
            coordinateClothRepository.save(coordinateCloth);

            // Cloth 관련 데이터 생성
            Cloth cloth3 =
                    Cloth.createCloth(
                            "clothImageUrl3",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            targetMember);
            clothRepository.save(cloth3);

            // MemberTerm 생성
            MemberTerm memberTerm = MemberTerm.createMemberTerm(targetMember, term, true);
            memberTermRepository.save(memberTerm);

            // CodiveNotification 생성
            CodiveNotification notification =
                    CodiveNotification.createCodiveNotification(
                            targetMember,
                            "testContent",
                            "notificationImageUrl",
                            "redirectInfo",
                            RedirectType.HISTORY_REDIRECT);
            codiveNotificationRepository.save(notification);

            // Follow 생성 (targetMember가 팔로우하는 경우)
            Follow follow = Follow.createFollow(targetMember, otherMember);
            followRepository.save(follow);

            // Follow 생성 (다른 회원이 targetMember를 팔로우하는 경우)
            Follow followToTarget = Follow.createFollow(otherMember, targetMember);
            followRepository.save(followToTarget);

            // PendingFollow 생성 (targetMember가 팔로우 요청한 경우)
            PendingFollow pendingFollow =
                    PendingFollow.createPendingFollow(targetMember, otherMember);
            pendingFollowRepository.save(pendingFollow);

            // PendingFollow 생성 (다른 회원이 targetMember에게 팔로우 요청한 경우)
            PendingFollow pendingFollowToTarget =
                    PendingFollow.createPendingFollow(otherMember, targetMember);
            pendingFollowRepository.save(pendingFollowToTarget);

            // Block 생성 (targetMember가 차단한 경우)
            Block block = Block.createBlock(targetMember, otherMember);
            blockRepository.save(block);

            // Block 생성 (다른 회원이 targetMember를 차단한 경우)
            Block blockTarget = Block.createBlock(otherMember, targetMember);
            blockRepository.save(blockTarget);

            // Report 생성 (targetMember가 신고한 경우)
            Report report =
                    Report.createReport(
                            history.getId(),
                            targetMember,
                            otherMember,
                            TargetType.HISTORY,
                            ReportReason.SEXUAL,
                            "testContent");
            reportRepository.save(report);

            // Report 생성 (다른 회원이 targetMember를 신고한 경우)
            Report reportTarget =
                    Report.createReport(
                            history.getId(),
                            otherMember,
                            targetMember,
                            TargetType.HISTORY,
                            ReportReason.SEXUAL,
                            "testContent");
            reportRepository.save(reportTarget);

            // RefreshToken 생성
            RefreshToken refreshToken =
                    RefreshToken.builder()
                            .memberId(targetMember.getId())
                            .token("testToken")
                            .build();
            refreshTokenRepository.save(refreshToken);
        }

        @Test
        void 유효한_요청이면_회원과_관련된_모든_데이터를_삭제하고_이미지_삭제_이벤트를_발행한다() {
            // when
            authService.withdrawMember();

            // then
            var events = applicationEvents.stream(ImagesDeleteEvent.class).toList();

            Assertions.assertAll(
                    // 회원 삭제 확인
                    () -> assertThat(memberRepository.findById(targetMemberId).isEmpty()).isTrue(),

                    // History 관련 데이터 삭제 확인
                    () -> assertThat(historyRepository.findAll()).isEmpty(),
                    () -> assertThat(historyImageRepository.findAll()).isEmpty(),
                    () -> assertThat(historyClothTagRepository.findAll()).isEmpty(),
                    () -> assertThat(historyHashtagRepository.findAll()).isEmpty(),
                    () -> assertThat(historyStyleRepository.findAll()).isEmpty(),

                    // MemberLike 삭제 확인
                    () -> assertThat(memberLikeRepository.findAll()).isEmpty(),

                    // Comment 삭제 확인
                    () -> assertThat(commentRepository.findAll()).isEmpty(),

                    // Coordinate 관련 데이터 삭제 확인
                    () -> assertThat(coordinateRepository.findAll()).isEmpty(),
                    () -> assertThat(coordinateClothRepository.findAll()).isEmpty(),

                    // LookBook 삭제 확인
                    () -> assertThat(lookBookRepository.findAll()).isEmpty(),

                    // Cloth 관련 데이터 삭제 확인
                    () -> assertThat(clothRepository.findAll()).isEmpty(),

                    // MemberTerm 삭제 확인
                    () -> assertThat(memberTermRepository.findAll()).isEmpty(),

                    // CodiveNotification 삭제 확인
                    () -> assertThat(codiveNotificationRepository.findAll()).isEmpty(),

                    // Follow 삭제 확인 (targetMember와 관련된 모든 Follow 삭제)
                    () -> assertThat(followRepository.findAll()).isEmpty(),

                    // PendingFollow 삭제 확인 (targetMember와 관련된 모든 PendingFollow 삭제)
                    () -> assertThat(pendingFollowRepository.findAll()).isEmpty(),

                    // Block 삭제 확인 (targetMember와 관련된 모든 Block 삭제)
                    () -> assertThat(blockRepository.findAll()).isEmpty(),

                    // Report 삭제 확인 (targetMember와 관련된 모든 Report 삭제)
                    () -> assertThat(reportRepository.findAll()).isEmpty(),

                    // RefreshToken 삭제 확인
                    () ->
                            assertThat(refreshTokenRepository.findById(targetMemberId).isEmpty())
                                    .isTrue(),

                    // 이미지 삭제 이벤트 발행 확인
                    () -> assertThat(events).hasSize(1),
                    () ->
                            assertThat(events.getFirst().imageUrls())
                                    .containsExactlyInAnyOrder(
                                            "profileImageUrl",
                                            "profileBackImageUrl",
                                            "historyImageUrl1",
                                            "historyImageUrl2",
                                            "coordinateImageUrl",
                                            "clothImageUrl1",
                                            "clothImageUrl2",
                                            "clothImageUrl3"),

                    // 마스터 데이터는 삭제되지 않았는지 확인
                    () -> assertThat(categoryRepository.findAll()).hasSize(1),
                    () -> assertThat(situationRepository.findAll()).hasSize(1),
                    () -> assertThat(styleRepository.findAll()).hasSize(2),
                    () -> assertThat(hashtagRepository.findAll()).hasSize(2),
                    () -> assertThat(termRepository.findAll()).hasSize(1),

                    // 다른 회원의 데이터는 유지되는지 확인
                    () ->
                            assertThat(memberRepository.findById(otherMemberId).isPresent())
                                    .isTrue());
        }
    }
}
