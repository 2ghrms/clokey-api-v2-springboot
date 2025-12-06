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
import org.clokey.domain.history.dto.request.HistoryCreateRequest;
import org.clokey.domain.history.dto.request.HistoryCreateRequest.ClothTag;
import org.clokey.domain.history.dto.request.HistoryCreateRequest.Payload;
import org.clokey.domain.history.dto.request.HistoryUpdateRequest;
import org.clokey.domain.history.dto.response.HistoryCreateResponse;
import org.clokey.domain.history.dto.response.SituationListResponse;
import org.clokey.domain.history.dto.response.StyleListResponse;
import org.clokey.domain.history.exception.HistoryErrorCode;
import org.clokey.domain.history.exception.SituationErrorCode;
import org.clokey.domain.history.exception.StyleErrorCode;
import org.clokey.domain.history.repository.*;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.util.MemberUtil;
import org.clokey.history.entity.*;
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
}
