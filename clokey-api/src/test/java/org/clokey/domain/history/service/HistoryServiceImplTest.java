package org.clokey.domain.history.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

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
import org.clokey.domain.history.dto.response.HistoryCreateResponse;
import org.clokey.domain.history.exception.SituationErrorCode;
import org.clokey.domain.history.exception.StyleErrorCode;
import org.clokey.domain.history.repository.*;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.util.MemberUtil;
import org.clokey.history.entity.Hashtag;
import org.clokey.history.entity.History;
import org.clokey.history.entity.HistoryHashtag;
import org.clokey.history.entity.HistoryImage;
import org.clokey.history.entity.HistoryStyle;
import org.clokey.history.entity.Situation;
import org.clokey.history.entity.Style;
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
}
