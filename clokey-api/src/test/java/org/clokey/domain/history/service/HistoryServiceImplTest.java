package org.clokey.domain.history.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.util.List;
import org.clokey.IntegrationTest;
import org.clokey.TransactionUtil;
import org.clokey.category.entity.Category;
import org.clokey.cloth.entity.Cloth;
import org.clokey.cloth.enums.Season;
import org.clokey.domain.category.repository.CategoryRepository;
import org.clokey.domain.cloth.exception.ClothErrorCode;
import org.clokey.domain.cloth.repository.ClothRepository;
import org.clokey.domain.comment.repository.CommentRepository;
import org.clokey.domain.history.dto.request.HistoryCreateRequest;
import org.clokey.domain.history.dto.request.HistoryCreateRequest.ClothTag;
import org.clokey.domain.history.dto.request.HistoryCreateRequest.Payload;
import org.clokey.domain.history.dto.request.HistoryUpdateRequest;
import org.clokey.domain.history.dto.response.DailyHistoryResponse;
import org.clokey.domain.history.dto.response.HistoryClothTagListResponse;
import org.clokey.domain.history.dto.response.HistoryCreateResponse;
import org.clokey.domain.history.dto.response.HistoryOwnershipCheckResponse;
import org.clokey.domain.history.dto.response.MonthlyHistoryResponse;
import org.clokey.domain.history.dto.response.SituationListResponse;
import org.clokey.domain.history.dto.response.StyleListResponse;
import org.clokey.domain.history.exception.HistoryErrorCode;
import org.clokey.domain.history.exception.SituationErrorCode;
import org.clokey.domain.history.exception.StyleErrorCode;
import org.clokey.domain.history.repository.*;
import org.clokey.domain.like.repository.MemberLikeRepository;
import org.clokey.domain.member.repository.BlockRepository;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.util.MemberUtil;
import org.clokey.history.entity.*;
import org.clokey.member.entity.Block;
import org.clokey.member.entity.Member;
import org.clokey.member.entity.OauthInfo;
import org.clokey.member.enums.OauthProvider;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class HistoryServiceImplTest extends IntegrationTest {

    @Autowired private TransactionUtil transactionUtil;

    @Autowired private MemberRepository memberRepository;
    @Autowired private HistoryService historyService;
    @Autowired private SituationRepository situationRepository;
    @Autowired private StyleRepository styleRepository;
    @Autowired private HistoryRepository historyRepository;
    @Autowired private HistoryImageRepository historyImageRepository;
    @Autowired private HistoryStyleRepository historyStyleRepository;
    @Autowired private HashtagRepository hashtagRepository;
    @Autowired private HistoryHashtagRepository historyHashtagRepository;
    @Autowired private HistoryClothTagRepository historyClothTagRepository;
    @Autowired private ClothRepository clothRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private MemberLikeRepository memberLikeRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private BlockRepository blockRepository;

    @MockitoBean private MemberUtil memberUtil;

    @Nested
    class 기록을_생성할_때 {

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

            Style style1 = Style.createStyle("testStyle1");
            Style style2 = Style.createStyle("testStyle2");
            Style style3 = Style.createStyle("testStyle3");
            styleRepository.saveAll(List.of(style1, style2, style3));

            Category category = Category.createCategory("testCategory", null);
            categoryRepository.save(category);

            Cloth cloth1 =
                    Cloth.createCloth(
                            "testImageUrl1", null, null, null, Season.SPRING, category, member1);
            Cloth cloth2 =
                    Cloth.createCloth(
                            "testImageUrl2", null, null, null, Season.SPRING, category, member1);
            Cloth cloth3 =
                    Cloth.createCloth(
                            "testImageUrl3", null, null, null, Season.SPRING, category, member2);

            clothRepository.saveAll(List.of(cloth1, cloth2, cloth3));

            hashtagRepository.saveAll(
                    List.of(
                            Hashtag.createHashtag("testhashtag1"),
                            Hashtag.createHashtag("testhashtag2")));
        }

        @Test
        void 유효한_요청이면_기록을_생성한다() {
            // given
            HistoryCreateRequest request =
                    new HistoryCreateRequest(
                            "testContent 1 ",
                            1L,
                            List.of(1L, 2L),
                            List.of("testHashtag1", "testHashtag2"),
                            List.of(
                                    new Payload(
                                            "testUrl1",
                                            List.of(
                                                    new ClothTag(1L, 0.25, 0.5),
                                                    new ClothTag(2L, 0.75, 0.33))),
                                    new Payload(
                                            "testUrl2", null // 태그 없는 이미지
                                            )));

            // when
            HistoryCreateResponse res = historyService.createHistory(request);

            // then
            History history =
                    transactionUtil.getResult(
                            () -> historyRepository.findById(res.historyId()).orElseThrow());

            assertThat(history)
                    .extracting(
                            h -> h.getMember().getId(),
                            h -> h.getSituation().getId(),
                            History::getContent)
                    .containsExactly(1L, 1L, "testContent 1");

            List<HistoryImage> images = historyImageRepository.findByHistoryId(history.getId());
            assertThat(images).hasSize(2);
            assertThat(images)
                    .extracting(HistoryImage::getImageUrl)
                    .containsExactlyInAnyOrder("testUrl1", "testUrl2");

            HistoryImage firstImage =
                    images.stream()
                            .filter(i -> "testUrl1".equals(i.getImageUrl()))
                            .findFirst()
                            .orElseThrow();

            List<org.clokey.history.entity.HistoryClothTag> tags =
                    historyClothTagRepository.findByHistoryImageId(firstImage.getId());
            assertThat(tags).hasSize(2);
            assertThat(tags)
                    .extracting(
                            t -> t.getCloth().getId(),
                            t -> t.getLocation().getLocationX(),
                            t -> t.getLocation().getLocationY())
                    .containsExactlyInAnyOrder(tuple(1L, 0.25, 0.5), tuple(2L, 0.75, 0.33));

            List<HistoryStyle> styles = historyStyleRepository.findByHistoryId(history.getId());
            assertThat(styles).hasSize(2);
            assertThat(styles)
                    .extracting(hs -> hs.getStyle().getId())
                    .containsExactlyInAnyOrder(1L, 2L);

            List<HistoryHashtag> historyHashtags =
                    historyHashtagRepository.findAllByHistoryIdWithHashtag(history.getId());
            assertThat(historyHashtags).hasSize(2);
        }

        @Test
        void 존재하지_않는_상황_ID를_포함하는_경우_예외가_발생한다() {
            // given
            HistoryCreateRequest request =
                    new HistoryCreateRequest(
                            "hi",
                            999L,
                            List.of(1L, 2L),
                            List.of("testHashtag1", "testHashtag2"),
                            List.of(
                                    new Payload(
                                            "testUrl1",
                                            List.of(
                                                    new ClothTag(1L, 0.25, 0.5),
                                                    new ClothTag(2L, 0.75, 0.33))),
                                    new Payload("testUrl2", null)));
            // when & then
            assertThatThrownBy(() -> historyService.createHistory(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(SituationErrorCode.SITUATION_NOT_FOUND.getMessage());
        }

        @Test
        void 존재하지_않는_스타일_ID를_포함하는_경우_예외가_발생한다() {
            // given
            HistoryCreateRequest request =
                    new HistoryCreateRequest(
                            "testContent 1 ",
                            1L,
                            List.of(1L, 10L),
                            List.of("testHashtag1", "testHashtag2"),
                            List.of(
                                    new Payload(
                                            "testUrl1",
                                            List.of(
                                                    new ClothTag(1L, 0.25, 0.5),
                                                    new ClothTag(2L, 0.75, 0.33))),
                                    new Payload("testUrl2", null)));
            // when & then
            assertThatThrownBy(() -> historyService.createHistory(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(StyleErrorCode.STYLE_NOT_FOUND.getMessage());
        }

        @Test
        void 이미지_태그가_존재하지_않는_옷을_참조하면_예외가_발생한다() {
            HistoryCreateRequest request =
                    new HistoryCreateRequest(
                            "testContent 1 ",
                            1L,
                            List.of(1L, 2L),
                            List.of("testHashtag1", "testHashtag2"),
                            List.of(
                                    new Payload("testUrl1", List.of(new ClothTag(4L, 0.25, 0.5))),
                                    new Payload("testUrl2", null)));
            assertThatThrownBy(() -> historyService.createHistory(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(ClothErrorCode.ClOTH_NOT_FOUND.getMessage());
        }

        @Test
        void 이미지_태그에_나의_옷이_아닌_옷이_포함된_경우_예외가_발생한다() {
            HistoryCreateRequest request =
                    new HistoryCreateRequest(
                            "testContent 1 ",
                            1L,
                            List.of(1L, 2L),
                            List.of("testHashtag1", "testHashtag2"),
                            List.of(
                                    new Payload("testUrl1", List.of(new ClothTag(3L, 0.25, 0.5))),
                                    new Payload("testUrl2", null)));
            assertThatThrownBy(() -> historyService.createHistory(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(ClothErrorCode.NOT_CLOTH_OWNER.getMessage());
        }

        @Test
        void 이미지_태그에_중복된_옷이_포함된_경우_예외가_발생한다() {
            HistoryCreateRequest request =
                    new HistoryCreateRequest(
                            "testContent 1 ",
                            1L,
                            List.of(1L, 2L),
                            List.of("testHashtag1", "testHashtag2"),
                            List.of(
                                    new Payload(
                                            "testUrl1",
                                            List.of(
                                                    new ClothTag(1L, 0.25, 0.5),
                                                    new ClothTag(1L, 0.75, 0.33))),
                                    new Payload("testUrl2", null)));
            assertThatThrownBy(() -> historyService.createHistory(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(ClothErrorCode.DUPLICATED_CLOTH.getMessage());
        }

        @Test
        void 존재하지_않는_해시태그를_포함하면_생성한다() {
            HistoryCreateRequest request =
                    new HistoryCreateRequest(
                            "testContent 1 ",
                            1L,
                            List.of(1L, 2L),
                            List.of("testHashtag1", "testHashtag2", "testHashtag3"),
                            List.of(
                                    new Payload(
                                            "testUrl1",
                                            List.of(
                                                    new ClothTag(1L, 0.25, 0.5),
                                                    new ClothTag(2L, 0.75, 0.33))),
                                    new Payload("testUrl2", null)));

            HistoryCreateResponse res = historyService.createHistory(request);
            List<HistoryHashtag> historyHashtags =
                    historyHashtagRepository.findAllByHistoryIdWithHashtag(res.historyId());
            assertThat(historyHashtags).hasSize(3);
            assertThat(historyHashtags)
                    .extracting(hh -> hh.getHashtag().getName())
                    .containsExactlyInAnyOrder("testhashtag1", "testhashtag2", "testhashtag3");
        }
    }

    @Nested
    class 기록을_수정할_때 {

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
            Situation situation2 = Situation.createSituation("testSituation2");
            situationRepository.saveAll(List.of(situation1, situation2));

            Style style1 = Style.createStyle("testStyle1");
            Style style2 = Style.createStyle("testStyle2");
            Style style3 = Style.createStyle("testStyle3");
            styleRepository.saveAll(List.of(style1, style2, style3));

            Category category = Category.createCategory("testCategory", null);
            categoryRepository.save(category);

            Cloth cloth1 =
                    Cloth.createCloth(
                            "testImageUrl1", null, null, null, Season.SPRING, category, member1);
            Cloth cloth2 =
                    Cloth.createCloth(
                            "testImageUrl2", null, null, null, Season.SPRING, category, member1);
            Cloth cloth3 =
                    Cloth.createCloth(
                            "testImageUrl2", null, null, null, Season.SPRING, category, member2);
            clothRepository.saveAll(List.of(cloth1, cloth2, cloth3));

            Hashtag hashtag1 = Hashtag.createHashtag("testhashtag1");
            Hashtag hashtag2 = Hashtag.createHashtag("testhashtag2");
            hashtagRepository.saveAll(List.of(hashtag1, hashtag2));

            History history =
                    History.createHistory(LocalDate.now(), "old content", member1, situation1);
            historyRepository.save(history);

            HistoryImage image1 = HistoryImage.createHistoryImage("image1", history);
            HistoryImage image2 = HistoryImage.createHistoryImage("image2", history);
            historyImageRepository.saveAll(List.of(image1, image2));

            HistoryClothTag tag1 = HistoryClothTag.createHistoryClothTag(image1, cloth1, 0.2, 0.3);
            HistoryClothTag tag2 = HistoryClothTag.createHistoryClothTag(image2, cloth2, 0.4, 0.5);
            historyClothTagRepository.bulkInsertHistoryClothTags(List.of(tag1, tag2));

            historyStyleRepository.bulkInsertHistoryStyles(
                    List.of(
                            HistoryStyle.createHistoryStyle(history, style1),
                            HistoryStyle.createHistoryStyle(history, style2)));

            historyHashtagRepository.bulkInsertHistoryHashtags(
                    List.of(HistoryHashtag.createHistoryHashtag(history, hashtag1)));
        }

        @Test
        void 유효한_요청이면_기록을_수정한다() {
            // given
            HistoryUpdateRequest request =
                    new HistoryUpdateRequest(
                            "new content ",
                            2L,
                            List.of(2L, 3L),
                            List.of("testHashtag2", "newHash"),
                            List.of(
                                    new HistoryUpdateRequest.Payload(
                                            "image1",
                                            List.of(
                                                    new HistoryUpdateRequest.ClothTag(
                                                            2L, 0.5, 0.6))),
                                    new HistoryUpdateRequest.Payload(
                                            "image3",
                                            List.of(
                                                    new HistoryUpdateRequest.ClothTag(
                                                            1L, 0.1, 0.2)))));

            // when
            historyService.updateHistory(1L, request);

            // then
            History updated =
                    transactionUtil.getResult(() -> historyRepository.findById(1L).orElseThrow());

            assertThat(updated.getContent()).isEqualTo("new content");
            assertThat(updated.getSituation().getId()).isEqualTo(2L);

            List<HistoryImage> images = historyImageRepository.findByHistoryId(1L);
            assertThat(images).hasSize(2);
            assertThat(images)
                    .extracting(HistoryImage::getImageUrl)
                    .containsExactlyInAnyOrder("image1", "image3");

            HistoryImage keptImage =
                    images.stream()
                            .filter(img -> "image1".equals(img.getImageUrl()))
                            .findFirst()
                            .orElseThrow();
            List<org.clokey.history.entity.HistoryClothTag> keptTags =
                    historyClothTagRepository.findByHistoryImageId(keptImage.getId());
            assertThat(keptTags)
                    .extracting(
                            t -> t.getCloth().getId(),
                            t -> t.getLocation().getLocationX(),
                            t -> t.getLocation().getLocationY())
                    .containsExactly(tuple(2L, 0.5, 0.6));

            HistoryImage newImage =
                    images.stream()
                            .filter(img -> "image3".equals(img.getImageUrl()))
                            .findFirst()
                            .orElseThrow();
            List<org.clokey.history.entity.HistoryClothTag> newTags =
                    historyClothTagRepository.findByHistoryImageId(newImage.getId());
            assertThat(newTags)
                    .extracting(
                            t -> t.getCloth().getId(),
                            t -> t.getLocation().getLocationX(),
                            t -> t.getLocation().getLocationY())
                    .containsExactly(tuple(1L, 0.1, 0.2));

            assertThat(historyStyleRepository.findByHistoryId(1L))
                    .extracting(hs -> hs.getStyle().getId())
                    .containsExactlyInAnyOrder(2L, 3L);

            List<HistoryHashtag> hashtags =
                    historyHashtagRepository.findAllByHistoryIdWithHashtag(1L);
            assertThat(hashtags)
                    .extracting(hh -> hh.getHashtag().getName())
                    .containsExactlyInAnyOrder("testhashtag2", "newhash");
        }

        @Test
        void 다른_사용자가_수정하면_권한_예외가_발생한다() {
            // given
            Member otherMember =
                    transactionUtil.getResult(() -> memberRepository.findById(2L).orElseThrow());
            given(memberUtil.getCurrentMember()).willReturn(otherMember);

            HistoryUpdateRequest request =
                    new HistoryUpdateRequest(
                            "new content ",
                            2L,
                            List.of(2L, 3L),
                            List.of("testHashtag2", "newHash"),
                            List.of(
                                    new HistoryUpdateRequest.Payload(
                                            "image1",
                                            List.of(
                                                    new HistoryUpdateRequest.ClothTag(
                                                            2L, 0.5, 0.6))),
                                    new HistoryUpdateRequest.Payload(
                                            "image3",
                                            List.of(
                                                    new HistoryUpdateRequest.ClothTag(
                                                            1L, 0.1, 0.2)))));

            // when & then
            assertThatThrownBy(() -> historyService.updateHistory(1L, request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(HistoryErrorCode.LIMITED_AUTHORITY.getMessage());
        }

        @Test
        void 존재하지_않는_historyId면_예외가_발생한다() {
            // given
            HistoryUpdateRequest request =
                    new HistoryUpdateRequest(
                            "new content ",
                            2L,
                            List.of(2L, 3L),
                            List.of("testHashtag2", "newHash"),
                            List.of(
                                    new HistoryUpdateRequest.Payload(
                                            "image1",
                                            List.of(
                                                    new HistoryUpdateRequest.ClothTag(
                                                            2L, 0.5, 0.6))),
                                    new HistoryUpdateRequest.Payload(
                                            "image3",
                                            List.of(
                                                    new HistoryUpdateRequest.ClothTag(
                                                            1L, 0.1, 0.2)))));

            // when & then
            assertThatThrownBy(() -> historyService.updateHistory(999L, request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(HistoryErrorCode.HISTORY_NOT_FOUND.getMessage());
        }

        @Test
        void 존재하지_않는_상황_ID를_포함하는_경우_예외가_발생한다() {
            // given
            HistoryUpdateRequest request =
                    new HistoryUpdateRequest(
                            "new content ",
                            999L,
                            List.of(2L, 3L),
                            List.of("testHashtag2", "newHash"),
                            List.of(
                                    new HistoryUpdateRequest.Payload(
                                            "image1",
                                            List.of(
                                                    new HistoryUpdateRequest.ClothTag(
                                                            2L, 0.5, 0.6))),
                                    new HistoryUpdateRequest.Payload(
                                            "image3",
                                            List.of(
                                                    new HistoryUpdateRequest.ClothTag(
                                                            1L, 0.1, 0.2)))));

            // when & then
            assertThatThrownBy(() -> historyService.updateHistory(1L, request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(SituationErrorCode.SITUATION_NOT_FOUND.getMessage());
        }

        @Test
        void 이미지_태그에_내_옷이_아닌_옷이_포함되면_예외가_발생한다() {
            // given
            HistoryUpdateRequest request =
                    new HistoryUpdateRequest(
                            "new content ",
                            2L,
                            List.of(2L, 3L),
                            List.of("testHashtag2", "newHash"),
                            List.of(
                                    new HistoryUpdateRequest.Payload(
                                            "image1",
                                            List.of(
                                                    new HistoryUpdateRequest.ClothTag(
                                                            3L, 0.5, 0.6))),
                                    new HistoryUpdateRequest.Payload(
                                            "image3",
                                            List.of(
                                                    new HistoryUpdateRequest.ClothTag(
                                                            1L, 0.1, 0.2)))));

            // when & then
            assertThatThrownBy(() -> historyService.updateHistory(1L, request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(ClothErrorCode.NOT_CLOTH_OWNER.getMessage());
        }
    }

    @Nested
    class 전체_스타일_목록을_조회할_때 {

        @BeforeEach
        void setUp() {
            Style style1 = Style.createStyle("testStyle1");
            Style style2 = Style.createStyle("testStyle2");
            Style style3 = Style.createStyle("testStyle3");
            styleRepository.saveAll(List.of(style1, style2, style3));
        }

        @Test
        void 전체_스타일_목록을_반환한다() {
            // when
            StyleListResponse response = historyService.getAllStyles();

            // then
            assertThat(response.contents()).hasSize(3);
            assertThat(response.contents())
                    .extracting(StyleListResponse.Content::styleId, StyleListResponse.Content::name)
                    .containsExactly(
                            tuple(1L, "testStyle1"),
                            tuple(2L, "testStyle2"),
                            tuple(3L, "testStyle3"));
        }
    }

    @Nested
    class 전체_상황_목록을_조회할_때 {

        @BeforeEach
        void setUp() {
            Situation situation1 = Situation.createSituation("testSituation1");
            Situation situation2 = Situation.createSituation("testSituation2");
            Situation situation3 = Situation.createSituation("testSituation3");
            situationRepository.saveAll(List.of(situation1, situation2, situation3));
        }

        @Test
        void 전체_상황_목록을_반환한다() {
            // when
            SituationListResponse response = historyService.getAllSituations();

            // then
            assertThat(response.contents()).hasSize(3);
            assertThat(response.contents())
                    .extracting(
                            SituationListResponse.Content::situationId,
                            SituationListResponse.Content::name)
                    .containsExactly(
                            tuple(1L, "testSituation1"),
                            tuple(2L, "testSituation2"),
                            tuple(3L, "testSituation3"));
        }
    }

    @Nested
    class 기록_이미지_옷_태그를_조회할_때 {

        @BeforeEach
        void setUp() {
            Member member1 =
                    Member.createMember(
                            "testEmail1",
                            "testClokeyId1",
                            "testNickName1",
                            OauthInfo.createOauthInfo("testOauthId1", OauthProvider.KAKAO));
            memberRepository.save(member1);
            given(memberUtil.getCurrentMember()).willReturn(member1);

            Situation situation1 = Situation.createSituation("testSituation1");
            situationRepository.save(situation1);

            Category category = Category.createCategory("testCategory", null);
            categoryRepository.save(category);

            Cloth cloth1 =
                    Cloth.createCloth(
                            "testClothImageUrl1",
                            null,
                            "testClothName1",
                            "testBrand1",
                            Season.SPRING,
                            category,
                            member1);
            Cloth cloth2 =
                    Cloth.createCloth(
                            "testClothImageUrl2",
                            null,
                            "testClothName2",
                            "testBrand2",
                            Season.SPRING,
                            category,
                            member1);
            clothRepository.saveAll(List.of(cloth1, cloth2));

            History history =
                    History.createHistory(LocalDate.now(), "testContent", member1, situation1);
            historyRepository.save(history);

            HistoryImage historyImage = HistoryImage.createHistoryImage("testImageUrl", history);
            historyImageRepository.save(historyImage);

            HistoryClothTag tag1 =
                    HistoryClothTag.createHistoryClothTag(historyImage, cloth1, 0.5, 0.7);
            HistoryClothTag tag2 =
                    HistoryClothTag.createHistoryClothTag(historyImage, cloth2, 0.3, 0.4);
            historyClothTagRepository.bulkInsertHistoryClothTags(List.of(tag1, tag2));
        }

        @Test
        void 유효한_요청이면_기록_이미지에_태그된_옷들의_정보를_반환한다() {
            // when
            HistoryClothTagListResponse response = historyService.getHistoryClothTags(1L);

            // then
            assertThat(response.payloads()).hasSize(2);
            assertThat(response.payloads())
                    .extracting(
                            HistoryClothTagListResponse.Payload::clothId,
                            HistoryClothTagListResponse.Payload::clothImageUrl,
                            HistoryClothTagListResponse.Payload::name,
                            HistoryClothTagListResponse.Payload::brand,
                            HistoryClothTagListResponse.Payload::locationX,
                            HistoryClothTagListResponse.Payload::locationY)
                    .containsExactlyInAnyOrder(
                            tuple(
                                    1L,
                                    "testClothImageUrl1",
                                    "testClothName1",
                                    "testBrand1",
                                    0.5,
                                    0.7),
                            tuple(
                                    2L,
                                    "testClothImageUrl2",
                                    "testClothName2",
                                    "testBrand2",
                                    0.3,
                                    0.4));
        }

        @Test
        void 존재하지_않는_기록_이미지_ID를_입력하면_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> historyService.getHistoryClothTags(999L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(HistoryErrorCode.HISTORY_IMAGE_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 월별_기록을_조회할_때 {

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
            member2.changeVisibility();

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
                            LocalDate.of(2025, 1, 1), "testContent1", member1, situation1);
            History history2 =
                    History.createHistory(
                            LocalDate.of(2025, 1, 15), "testContent2", member1, situation1);
            historyRepository.saveAll(List.of(history1, history2));

            HistoryImage image1 = HistoryImage.createHistoryImage("testImageUrl1", history1);
            HistoryImage image2 = HistoryImage.createHistoryImage("testImageUrl2", history2);
            historyImageRepository.saveAll(List.of(image1, image2));
        }

        @Test
        void 유효한_요청이면_월별_기록을_반환한다() {
            // when
            MonthlyHistoryResponse response = historyService.getMonthlyHistory(1L, 2025, 1);

            // then
            assertThat(response.payloads()).hasSize(2);
            assertThat(response.payloads())
                    .extracting(
                            MonthlyHistoryResponse.Payload::historyId,
                            MonthlyHistoryResponse.Payload::firstImageUrl)
                    .containsExactly(tuple(1L, "testImageUrl1"), tuple(2L, "testImageUrl2"));
        }

        @Test
        void 존재하지_않는_회원_ID를_입력하면_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> historyService.getMonthlyHistory(999L, 2025, 1))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(
                            org.clokey.domain.member.exception.MemberErrorCode.MEMBER_NOT_FOUND
                                    .getMessage());
        }

        @Test
        void 비공개_회원이고_본인이_아닌_경우_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> historyService.getMonthlyHistory(2L, 2025, 1))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(HistoryErrorCode.LIMITED_AUTHORITY.getMessage());
        }

        @Test
        void 차단한_회원의_월별_기록을_조회하면_예외가_발생한다() {
            // given
            Member blocker =
                    transactionUtil.getResult(() -> memberRepository.findById(1L).orElseThrow());
            Member blocked =
                    transactionUtil.getResult(() -> memberRepository.findById(3L).orElseThrow());
            Block block = Block.createBlock(blocker, blocked);
            blockRepository.save(block);

            // when & then
            assertThatThrownBy(() -> historyService.getMonthlyHistory(3L, 2025, 1))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(HistoryErrorCode.BLOCKED_AUTHORITY.getMessage());
        }

        @Test
        void 차단당한_회원의_월별_기록을_조회하면_예외가_발생한다() {
            // given
            Member blocker =
                    transactionUtil.getResult(() -> memberRepository.findById(3L).orElseThrow());
            Member blocked =
                    transactionUtil.getResult(() -> memberRepository.findById(1L).orElseThrow());
            Block block = Block.createBlock(blocker, blocked);
            blockRepository.save(block);

            // when & then
            assertThatThrownBy(() -> historyService.getMonthlyHistory(3L, 2025, 1))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(HistoryErrorCode.BLOCKED_AUTHORITY.getMessage());
        }
    }

    @Nested
    class 기록_소유_여부를_확인할_때 {

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

            History history =
                    History.createHistory(LocalDate.now(), "testContent", member1, situation1);
            historyRepository.save(history);
        }

        @Test
        void 유효한_요청이면_기록_소유_여부를_반환한다() {
            // when
            HistoryOwnershipCheckResponse response = historyService.checkHistoryOwnership(1L);

            // then
            assertThat(response.isOwner()).isTrue();
        }

        @Test
        void 존재하지_않는_기록_ID를_입력하면_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> historyService.checkHistoryOwnership(999L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(HistoryErrorCode.HISTORY_NOT_FOUND.getMessage());
        }

        @Test
        void 다른_사용자의_기록을_확인하면_false를_반환한다() {
            // given
            Member otherMember =
                    transactionUtil.getResult(() -> memberRepository.findById(2L).orElseThrow());
            given(memberUtil.getCurrentMember()).willReturn(otherMember);

            // when
            HistoryOwnershipCheckResponse response = historyService.checkHistoryOwnership(1L);

            // then
            assertThat(response.isOwner()).isFalse();
        }
    }

    @Nested
    class 일별_기록을_조회할_때 {

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
            member2.changeVisibility();

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

            Style style1 = Style.createStyle("testStyle1");
            Style style2 = Style.createStyle("testStyle2");
            styleRepository.saveAll(List.of(style1, style2));

            History history1 =
                    History.createHistory(LocalDate.now(), "testContent1", member1, situation1);
            History history2 =
                    History.createHistory(LocalDate.now(), "testContent2", member2, situation1);
            History history3 =
                    History.createHistory(LocalDate.now(), "testContent3", member3, situation1);
            historyRepository.saveAll(List.of(history1, history2, history3));

            HistoryImage image1 = HistoryImage.createHistoryImage("testImageUrl1", history1);
            HistoryImage image2 = HistoryImage.createHistoryImage("testImageUrl2", history1);
            HistoryImage image3 = HistoryImage.createHistoryImage("testImageUrl3", history3);
            historyImageRepository.saveAll(List.of(image1, image2, image3));

            historyStyleRepository.bulkInsertHistoryStyles(
                    List.of(
                            HistoryStyle.createHistoryStyle(history1, style1),
                            HistoryStyle.createHistoryStyle(history1, style2)));
        }

        @Test
        void 유효한_요청이면_일별_기록을_반환한다() {
            // when
            DailyHistoryResponse response = historyService.getDailyHistory(1L);

            // then
            assertThat(response)
                    .extracting(
                            DailyHistoryResponse::memberId,
                            DailyHistoryResponse::historyDate,
                            DailyHistoryResponse::situationId,
                            DailyHistoryResponse::situationName,
                            DailyHistoryResponse::likeCount,
                            DailyHistoryResponse::commentCount)
                    .containsExactly(1L, LocalDate.now(), 1L, "testSituation1", 0L, 0L);

            assertThat(response.images()).hasSize(2);
            assertThat(response.images())
                    .extracting(
                            DailyHistoryResponse.ImagePayload::imageId,
                            DailyHistoryResponse.ImagePayload::imageUrl)
                    .containsExactlyInAnyOrder(
                            tuple(1L, "testImageUrl1"), tuple(2L, "testImageUrl2"));

            assertThat(response.styles()).hasSize(2);
            assertThat(response.styles())
                    .extracting(
                            DailyHistoryResponse.StylePayload::styleId,
                            DailyHistoryResponse.StylePayload::styleName)
                    .containsExactlyInAnyOrder(tuple(1L, "testStyle1"), tuple(2L, "testStyle2"));
        }

        @Test
        void 존재하지_않는_기록_ID를_입력하면_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> historyService.getDailyHistory(999L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(HistoryErrorCode.HISTORY_NOT_FOUND.getMessage());
        }

        @Test
        void 비공개_회원의_기록이고_본인이_아닌_경우_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> historyService.getDailyHistory(2L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(HistoryErrorCode.LIMITED_AUTHORITY.getMessage());
        }

        @Test
        void 차단한_회원의_기록을_조회하면_예외가_발생한다() {
            // given
            Member blocker =
                    transactionUtil.getResult(() -> memberRepository.findById(1L).orElseThrow());
            Member blocked =
                    transactionUtil.getResult(() -> memberRepository.findById(3L).orElseThrow());
            Block block = Block.createBlock(blocker, blocked);
            blockRepository.save(block);

            // when & then
            assertThatThrownBy(() -> historyService.getDailyHistory(3L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(HistoryErrorCode.BLOCKED_AUTHORITY.getMessage());
        }

        @Test
        void 차단당한_회원의_기록을_조회하면_예외가_발생한다() {
            // given
            Member blocker =
                    transactionUtil.getResult(() -> memberRepository.findById(3L).orElseThrow());
            Member blocked =
                    transactionUtil.getResult(() -> memberRepository.findById(1L).orElseThrow());
            Block block = Block.createBlock(blocker, blocked);
            blockRepository.save(block);

            // when & then
            assertThatThrownBy(() -> historyService.getDailyHistory(3L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(HistoryErrorCode.BLOCKED_AUTHORITY.getMessage());
        }
    }
}
