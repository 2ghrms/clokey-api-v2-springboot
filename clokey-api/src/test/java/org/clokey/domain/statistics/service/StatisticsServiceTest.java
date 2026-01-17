package org.clokey.domain.statistics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.util.List;
import org.clokey.IntegrationTest;
import org.clokey.category.entity.Category;
import org.clokey.cloth.entity.Cloth;
import org.clokey.cloth.enums.Season;
import org.clokey.coordinate.entity.Coordinate;
import org.clokey.coordinate.entity.CoordinateCloth;
import org.clokey.domain.category.exception.CategoryErrorCode;
import org.clokey.domain.category.repository.CategoryRepository;
import org.clokey.domain.cloth.repository.ClothRepository;
import org.clokey.domain.coordinate.repository.CoordinateClothRepository;
import org.clokey.domain.coordinate.repository.CoordinateRepository;
import org.clokey.domain.history.repository.HistoryClothTagRepository;
import org.clokey.domain.history.repository.HistoryImageRepository;
import org.clokey.domain.history.repository.HistoryRepository;
import org.clokey.domain.history.repository.SituationRepository;
import org.clokey.domain.lookbook.repository.LookBookRepository;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.domain.statistics.dto.response.ClosetUtilizationResponse;
import org.clokey.domain.statistics.dto.response.FavoriteCategoryItemsResponse;
import org.clokey.domain.statistics.dto.response.FavoriteItemsResponse;
import org.clokey.domain.statistics.dto.response.StatisticsCheckConditionResponse;
import org.clokey.domain.statistics.exception.StatisticsErrorCode;
import org.clokey.domain.statistics.repository.StatisticsRepositoryCustom;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.util.MemberUtil;
import org.clokey.history.entity.History;
import org.clokey.history.entity.HistoryClothTag;
import org.clokey.history.entity.HistoryImage;
import org.clokey.history.entity.Situation;
import org.clokey.lookbook.entity.LookBook;
import org.clokey.member.entity.Member;
import org.clokey.member.entity.OauthInfo;
import org.clokey.member.enums.OauthProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class StatisticsServiceTest extends IntegrationTest {

    @Autowired private StatisticsService statisticsService;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ClothRepository clothRepository;
    @Autowired private HistoryRepository historyRepository;
    @Autowired private HistoryImageRepository historyImageRepository;
    @Autowired private HistoryClothTagRepository historyClothTagRepository;
    @Autowired private CoordinateRepository coordinateRepository;
    @Autowired private CoordinateClothRepository coordinateClothRepository;
    @Autowired private LookBookRepository lookBookRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private SituationRepository situationRepository;

    @MockitoBean private MemberUtil memberUtil;
    @Autowired private StatisticsRepositoryCustom statisticsRepositoryCustom;

    @Nested
    class 통계_최소_조건을_확인할_때 {

        @BeforeEach
        void setUp() {
            Member member =
                    Member.createMember(
                            "testEmail",
                            "testClokeyId",
                            "testNickName",
                            OauthInfo.createOauthInfo("testOauthId", OauthProvider.KAKAO));
            memberRepository.save(member);

            Category category = Category.createCategory("testCategoryName", null);
            categoryRepository.save(category);

            Situation situation = Situation.createSituation("testSituationName");
            situationRepository.save(situation);

            given(memberUtil.getCurrentMember()).willReturn(member);
        }

        @Test
        void 기록_옷_코디가_모두_있으면_통계_집계가_가능하다() {
            // given
            Member member = memberUtil.getCurrentMember();
            Category category = categoryRepository.findById(1L).orElseThrow();
            Situation situation = situationRepository.findById(1L).orElseThrow();

            Cloth cloth =
                    Cloth.createCloth(
                            "testImageUrl1",
                            null,
                            "testName1",
                            "testBrand1",
                            List.of(Season.SPRING),
                            category,
                            member);
            clothRepository.save(cloth);

            History history =
                    History.createHistory(LocalDate.now(), "testMemo1", member, situation);
            historyRepository.save(history);

            Coordinate coordinate = Coordinate.createDailyCoordinate("testImageUrl1", member);
            coordinateRepository.save(coordinate);

            // when
            StatisticsCheckConditionResponse response =
                    statisticsService.checkStatisticsCondition();

            // then
            assertThat(response.canAggregate()).isTrue();
        }

        @Test
        void 기록이_없으면_통계_집계가_불가능하다() {
            // given
            Member member = memberUtil.getCurrentMember();
            Category category = categoryRepository.findById(1L).orElseThrow();

            Cloth cloth =
                    Cloth.createCloth(
                            "testImageUrl1",
                            null,
                            "testName1",
                            "testBrand1",
                            List.of(Season.SPRING),
                            category,
                            member);
            clothRepository.save(cloth);

            Coordinate coordinate = Coordinate.createDailyCoordinate("testImageUrl1", member);
            coordinateRepository.save(coordinate);

            // when
            StatisticsCheckConditionResponse response =
                    statisticsService.checkStatisticsCondition();

            // then
            assertThat(response.canAggregate()).isFalse();
        }

        @Test
        void 옷이_없으면_통계_집계가_불가능하다() {
            // given
            Member member = memberUtil.getCurrentMember();
            Situation situation = situationRepository.findById(1L).orElseThrow();

            History history =
                    History.createHistory(LocalDate.now(), "testMemo1", member, situation);
            historyRepository.save(history);

            Coordinate coordinate = Coordinate.createDailyCoordinate("testImageUrl1", member);
            coordinateRepository.save(coordinate);

            // when
            StatisticsCheckConditionResponse response =
                    statisticsService.checkStatisticsCondition();

            // then
            assertThat(response.canAggregate()).isFalse();
        }

        @Test
        void 코디가_없으면_통계_집계가_불가능하다() {
            // given
            Member member = memberUtil.getCurrentMember();
            Category category = categoryRepository.findById(1L).orElseThrow();
            Situation situation = situationRepository.findById(1L).orElseThrow();

            Cloth cloth =
                    Cloth.createCloth(
                            "testImageUrl1",
                            null,
                            "testName1",
                            "testBrand1",
                            List.of(Season.SPRING),
                            category,
                            member);
            clothRepository.save(cloth);

            History history =
                    History.createHistory(LocalDate.now(), "testMemo1", member, situation);
            historyRepository.save(history);

            // when
            StatisticsCheckConditionResponse response =
                    statisticsService.checkStatisticsCondition();

            // then
            assertThat(response.canAggregate()).isFalse();
        }

        @Test
        void 아무것도_없으면_통계_집계가_불가능하다() {
            // when
            StatisticsCheckConditionResponse response =
                    statisticsService.checkStatisticsCondition();

            // then
            assertThat(response.canAggregate()).isFalse();
        }
    }

    @Nested
    class 카테고리별_최애_아이템을_조회할_때 {

        @BeforeEach
        void setUp() {
            Member member =
                    Member.createMember(
                            "testEmail",
                            "testClokeyId",
                            "testNickName",
                            OauthInfo.createOauthInfo("testOauthId", OauthProvider.KAKAO));
            memberRepository.save(member);

            Category parentCategory = Category.createCategory("testParentCategoryName", null);
            Category childCategory1 =
                    Category.createCategory("testChildCategoryName1", parentCategory);
            Category childCategory2 =
                    Category.createCategory("testChildCategoryName2", parentCategory);
            Category childCategory3 =
                    Category.createCategory("testChildCategoryName3", parentCategory);
            Category childCategory4 =
                    Category.createCategory("testChildCategoryName4", parentCategory);
            Category childCategory5 =
                    Category.createCategory("testChildCategoryName5", parentCategory);

            categoryRepository.saveAll(
                    List.of(
                            parentCategory,
                            childCategory1,
                            childCategory2,
                            childCategory3,
                            childCategory4,
                            childCategory5));

            given(memberUtil.getCurrentMember()).willReturn(member);
        }

        @Test
        void 유효한_요청이면_카테고리별_최애_아이템을_반환한다() {
            // given
            Member member = memberUtil.getCurrentMember();
            Category parentCategory = categoryRepository.findById(1L).orElseThrow();
            Category childCategory1 = categoryRepository.findById(2L).orElseThrow();
            Category childCategory2 = categoryRepository.findById(3L).orElseThrow();
            Category childCategory3 = categoryRepository.findById(4L).orElseThrow();

            List<Cloth> clothes =
                    List.of(
                            // 카테고리 1 5개
                            Cloth.createCloth(
                                    "testImageUrl1",
                                    null,
                                    "testName1",
                                    "testBrand1",
                                    List.of(Season.SPRING),
                                    childCategory1,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl2",
                                    null,
                                    "testName2",
                                    "testBrand2",
                                    List.of(Season.SPRING),
                                    childCategory1,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl3",
                                    null,
                                    "testName3",
                                    "testBrand3",
                                    List.of(Season.SPRING),
                                    childCategory1,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl4",
                                    null,
                                    "testName4",
                                    "testBrand4",
                                    List.of(Season.SPRING),
                                    childCategory1,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl5",
                                    null,
                                    "testName5",
                                    "testBrand5",
                                    List.of(Season.SPRING),
                                    childCategory1,
                                    member),
                            // 카테고리 2 3개
                            Cloth.createCloth(
                                    "testImageUrl6",
                                    null,
                                    "testName6",
                                    "testBrand6",
                                    List.of(Season.SPRING),
                                    childCategory2,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl7",
                                    null,
                                    "testName7",
                                    "testBrand7",
                                    List.of(Season.SPRING),
                                    childCategory2,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl8",
                                    null,
                                    "testName8",
                                    "testBrand8",
                                    List.of(Season.SPRING),
                                    childCategory2,
                                    member),
                            // 카테고리 3 2개
                            Cloth.createCloth(
                                    "testImageUrl9",
                                    null,
                                    "testName9",
                                    "testBrand9",
                                    List.of(Season.SPRING),
                                    childCategory3,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl10",
                                    null,
                                    "testName10",
                                    "testBrand10",
                                    List.of(Season.SPRING),
                                    childCategory3,
                                    member));
            clothRepository.saveAll(clothes);

            // when
            FavoriteCategoryItemsResponse response =
                    statisticsService.getFavoriteCategoryItems(parentCategory.getId());

            // then
            assertThat(response.payloads()).hasSize(3);
            assertThat(response.payloads())
                    .extracting("categoryId", "occupancyRate", "clothCount")
                    .containsExactly(
                            tuple(childCategory1.getId(), 0.5, 5L),
                            tuple(childCategory2.getId(), 0.3, 3L),
                            tuple(childCategory3.getId(), 0.2, 2L));
        }

        @Test
        void 카테고리별_최애_아이템이_4개일_때_모두_반환한다() {
            // given
            Member member = memberUtil.getCurrentMember();
            Category parentCategory = categoryRepository.findById(1L).orElseThrow();
            Category childCategory1 = categoryRepository.findById(2L).orElseThrow();
            Category childCategory2 = categoryRepository.findById(3L).orElseThrow();
            Category childCategory3 = categoryRepository.findById(4L).orElseThrow();
            Category childCategory4 = categoryRepository.findById(5L).orElseThrow();

            List<Cloth> clothes =
                    List.of(
                            // 카테고리 1 5개
                            Cloth.createCloth(
                                    "testImageUrl1",
                                    null,
                                    "testName1",
                                    "testBrand1",
                                    List.of(Season.SPRING),
                                    childCategory1,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl2",
                                    null,
                                    "testName2",
                                    "testBrand2",
                                    List.of(Season.SPRING),
                                    childCategory1,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl3",
                                    null,
                                    "testName3",
                                    "testBrand3",
                                    List.of(Season.SPRING),
                                    childCategory1,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl4",
                                    null,
                                    "testName4",
                                    "testBrand4",
                                    List.of(Season.SPRING),
                                    childCategory1,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl5",
                                    null,
                                    "testName5",
                                    "testBrand5",
                                    List.of(Season.SPRING),
                                    childCategory1,
                                    member),
                            // 카테고리 2 3개
                            Cloth.createCloth(
                                    "testImageUrl6",
                                    null,
                                    "testName6",
                                    "testBrand6",
                                    List.of(Season.SPRING),
                                    childCategory2,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl7",
                                    null,
                                    "testName7",
                                    "testBrand7",
                                    List.of(Season.SPRING),
                                    childCategory2,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl8",
                                    null,
                                    "testName8",
                                    "testBrand8",
                                    List.of(Season.SPRING),
                                    childCategory2,
                                    member),
                            // 카테고리 3 2개
                            Cloth.createCloth(
                                    "testImageUrl9",
                                    null,
                                    "testName9",
                                    "testBrand9",
                                    List.of(Season.SPRING),
                                    childCategory3,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl10",
                                    null,
                                    "testName10",
                                    "testBrand10",
                                    List.of(Season.SPRING),
                                    childCategory3,
                                    member),
                            // 카테고리 4 1개
                            Cloth.createCloth(
                                    "testImageUrl11",
                                    null,
                                    "testName11",
                                    "testBrand11",
                                    List.of(Season.SPRING),
                                    childCategory4,
                                    member));
            clothRepository.saveAll(clothes);

            // when
            FavoriteCategoryItemsResponse response =
                    statisticsService.getFavoriteCategoryItems(parentCategory.getId());

            // then
            assertThat(response.payloads()).hasSize(4);
            assertThat(response.payloads())
                    .extracting("categoryId")
                    .containsExactly(
                            childCategory1.getId(),
                            childCategory2.getId(),
                            childCategory3.getId(),
                            childCategory4.getId());
        }

        @Test
        void 카테고리별_최애_아이템이_5개_이상일_때_탑3와_기타를_반환한다() {
            // given
            Member member = memberUtil.getCurrentMember();
            Category parentCategory = categoryRepository.findById(1L).orElseThrow();
            Category childCategory1 = categoryRepository.findById(2L).orElseThrow();
            Category childCategory2 = categoryRepository.findById(3L).orElseThrow();
            Category childCategory3 = categoryRepository.findById(4L).orElseThrow();
            Category childCategory4 = categoryRepository.findById(5L).orElseThrow();
            Category childCategory5 = categoryRepository.findById(6L).orElseThrow();

            List<Cloth> clothes =
                    List.of(
                            // 카테고리 1 5개
                            Cloth.createCloth(
                                    "testImageUrl1",
                                    null,
                                    "testName1",
                                    "testBrand1",
                                    List.of(Season.SPRING),
                                    childCategory1,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl2",
                                    null,
                                    "testName2",
                                    "testBrand2",
                                    List.of(Season.SPRING),
                                    childCategory1,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl3",
                                    null,
                                    "testName3",
                                    "testBrand3",
                                    List.of(Season.SPRING),
                                    childCategory1,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl4",
                                    null,
                                    "testName4",
                                    "testBrand4",
                                    List.of(Season.SPRING),
                                    childCategory1,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl5",
                                    null,
                                    "testName5",
                                    "testBrand5",
                                    List.of(Season.SPRING),
                                    childCategory1,
                                    member),
                            // 카테고리 2 3개
                            Cloth.createCloth(
                                    "testImageUrl6",
                                    null,
                                    "testName6",
                                    "testBrand6",
                                    List.of(Season.SPRING),
                                    childCategory2,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl7",
                                    null,
                                    "testName7",
                                    "testBrand7",
                                    List.of(Season.SPRING),
                                    childCategory2,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl8",
                                    null,
                                    "testName8",
                                    "testBrand8",
                                    List.of(Season.SPRING),
                                    childCategory2,
                                    member),
                            // 카테고리 3 2개
                            Cloth.createCloth(
                                    "testImageUrl9",
                                    null,
                                    "testName9",
                                    "testBrand9",
                                    List.of(Season.SPRING),
                                    childCategory3,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl10",
                                    null,
                                    "testName10",
                                    "testBrand10",
                                    List.of(Season.SPRING),
                                    childCategory3,
                                    member),
                            // 카테고리 4 1개
                            Cloth.createCloth(
                                    "testImageUrl11",
                                    null,
                                    "testName11",
                                    "testBrand11",
                                    List.of(Season.SPRING),
                                    childCategory4,
                                    member),
                            // 카테고리 5 1개
                            Cloth.createCloth(
                                    "testImageUrl12",
                                    null,
                                    "testName12",
                                    "testBrand12",
                                    List.of(Season.SPRING),
                                    childCategory5,
                                    member));
            clothRepository.saveAll(clothes);

            // when
            FavoriteCategoryItemsResponse response =
                    statisticsService.getFavoriteCategoryItems(parentCategory.getId());

            // then
            assertThat(response.payloads()).hasSize(4);
            assertThat(response.payloads())
                    .extracting("categoryId", "clothCount")
                    .containsExactly(
                            tuple(childCategory1.getId(), 5L),
                            tuple(childCategory2.getId(), 3L),
                            tuple(childCategory3.getId(), 2L),
                            tuple(null, 2L)); // 1 + 1
        }

        @Test
        void 카테고리별_최애_아이템이_없을_때_빈_리스트를_반환한다() {
            // given
            Member member = memberUtil.getCurrentMember();
            Category parentCategory = categoryRepository.findById(1L).orElseThrow();

            // when
            FavoriteCategoryItemsResponse response =
                    statisticsService.getFavoriteCategoryItems(parentCategory.getId());

            // then
            assertThat(response.payloads()).isEmpty();
        }

        @Test
        void 카테고리가_존재하지_않으면_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> statisticsService.getFavoriteCategoryItems(999L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CategoryErrorCode.CATEGORY_NOT_FOUND.getMessage());
        }

        @Test
        void 카테고리가_1차_카테고리가_아니면_예외가_발생한다() {
            // given
            Category childCategory = categoryRepository.findById(2L).orElseThrow();

            // when & then
            assertThatThrownBy(
                            () -> statisticsService.getFavoriteCategoryItems(childCategory.getId()))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(StatisticsErrorCode.NOT_PARENT_CATEGORY.getMessage());
        }
    }

    @Nested
    class 옷장_아이템_통계를_조회할_때 {

        @BeforeEach
        void setUp() {
            Member member =
                    Member.createMember(
                            "testEmail",
                            "testClokeyId",
                            "testNickName",
                            OauthInfo.createOauthInfo("testOauthId", OauthProvider.KAKAO));
            memberRepository.save(member);

            Category category1 = Category.createCategory("testCategoryName1", null);
            Category category2 = Category.createCategory("testCategoryName2", null);
            Category category3 = Category.createCategory("testCategoryName3", null);
            categoryRepository.saveAll(List.of(category1, category2, category3));

            given(memberUtil.getCurrentMember()).willReturn(member);
        }

        @Test
        void 유효한_요청이면_옷장_아이템_통계를_반환한다() {
            // given
            Member member = memberUtil.getCurrentMember();
            Category category1 = categoryRepository.findById(1L).orElseThrow();
            Category category2 = categoryRepository.findById(2L).orElseThrow();
            Category category3 = categoryRepository.findById(3L).orElseThrow();

            List<Cloth> clothes =
                    List.of(
                            // 카테고리 1 5개
                            Cloth.createCloth(
                                    "testImageUrl1",
                                    null,
                                    "testName1",
                                    "testBrand1",
                                    List.of(Season.SPRING),
                                    category1,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl2",
                                    null,
                                    "testName2",
                                    "testBrand2",
                                    List.of(Season.SPRING),
                                    category1,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl3",
                                    null,
                                    "testName3",
                                    "testBrand3",
                                    List.of(Season.SPRING),
                                    category1,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl4",
                                    null,
                                    "testName4",
                                    "testBrand4",
                                    List.of(Season.SPRING),
                                    category1,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl5",
                                    null,
                                    "testName5",
                                    "testBrand5",
                                    List.of(Season.SPRING),
                                    category1,
                                    member),
                            // 카테고리 2 3개
                            Cloth.createCloth(
                                    "testImageUrl6",
                                    null,
                                    "testName6",
                                    "testBrand6",
                                    List.of(Season.SPRING),
                                    category2,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl7",
                                    null,
                                    "testName7",
                                    "testBrand7",
                                    List.of(Season.SPRING),
                                    category2,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl8",
                                    null,
                                    "testName8",
                                    "testBrand8",
                                    List.of(Season.SPRING),
                                    category2,
                                    member),
                            // 카테고리 3 2개
                            Cloth.createCloth(
                                    "testImageUrl9",
                                    null,
                                    "testName9",
                                    "testBrand9",
                                    List.of(Season.SPRING),
                                    category3,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl10",
                                    null,
                                    "testName10",
                                    "testBrand10",
                                    List.of(Season.SPRING),
                                    category3,
                                    member));
            clothRepository.saveAll(clothes);

            // when
            FavoriteItemsResponse response = statisticsService.getFavoriteItems();

            // then
            assertThat(response.payloads()).hasSize(3);
            assertThat(response.payloads())
                    .extracting("categoryId", "clothCount")
                    .containsExactly(
                            tuple(category1.getId(), 5L),
                            tuple(category2.getId(), 3L),
                            tuple(category3.getId(), 2L));
        }

        @Test
        void 옷장_아이템의_카테고리가_5개일_때_모두_반환한다() {
            // given
            Member member = memberUtil.getCurrentMember();
            Category category1 = categoryRepository.findById(1L).orElseThrow();
            Category category2 = categoryRepository.findById(2L).orElseThrow();
            Category category3 = categoryRepository.findById(3L).orElseThrow();

            Category category4 = Category.createCategory("testCategoryName4", null);
            Category category5 = Category.createCategory("testCategoryName5", null);
            categoryRepository.saveAll(List.of(category4, category5));

            List<Cloth> clothes =
                    List.of(
                            // 카테고리 1 5개
                            Cloth.createCloth(
                                    "testImageUrl1",
                                    null,
                                    "testName1",
                                    "testBrand1",
                                    List.of(Season.SPRING),
                                    category1,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl2",
                                    null,
                                    "testName2",
                                    "testBrand2",
                                    List.of(Season.SPRING),
                                    category1,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl3",
                                    null,
                                    "testName3",
                                    "testBrand3",
                                    List.of(Season.SPRING),
                                    category1,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl4",
                                    null,
                                    "testName4",
                                    "testBrand4",
                                    List.of(Season.SPRING),
                                    category1,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl5",
                                    null,
                                    "testName5",
                                    "testBrand5",
                                    List.of(Season.SPRING),
                                    category1,
                                    member),
                            // 카테고리 2 4개
                            Cloth.createCloth(
                                    "testImageUrl6",
                                    null,
                                    "testName6",
                                    "testBrand6",
                                    List.of(Season.SPRING),
                                    category2,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl7",
                                    null,
                                    "testName7",
                                    "testBrand7",
                                    List.of(Season.SPRING),
                                    category2,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl8",
                                    null,
                                    "testName8",
                                    "testBrand8",
                                    List.of(Season.SPRING),
                                    category2,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl9",
                                    null,
                                    "testName9",
                                    "testBrand9",
                                    List.of(Season.SPRING),
                                    category2,
                                    member),
                            // 카테고리 3 3개
                            Cloth.createCloth(
                                    "testImageUrl10",
                                    null,
                                    "testName10",
                                    "testBrand10",
                                    List.of(Season.SPRING),
                                    category3,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl11",
                                    null,
                                    "testName11",
                                    "testBrand11",
                                    List.of(Season.SPRING),
                                    category3,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl12",
                                    null,
                                    "testName12",
                                    "testBrand12",
                                    List.of(Season.SPRING),
                                    category3,
                                    member),
                            // 카테고리 4 2개
                            Cloth.createCloth(
                                    "testImageUrl13",
                                    null,
                                    "testName13",
                                    "testBrand13",
                                    List.of(Season.SPRING),
                                    category4,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl14",
                                    null,
                                    "testName14",
                                    "testBrand14",
                                    List.of(Season.SPRING),
                                    category4,
                                    member),
                            // 카테고리 5 1개
                            Cloth.createCloth(
                                    "testImageUrl15",
                                    null,
                                    "testName15",
                                    "testBrand15",
                                    List.of(Season.SPRING),
                                    category5,
                                    member));
            clothRepository.saveAll(clothes);

            // when
            FavoriteItemsResponse response = statisticsService.getFavoriteItems();

            // then
            assertThat(response.payloads()).hasSize(5);
            assertThat(response.payloads())
                    .extracting("categoryId", "clothCount")
                    .containsExactly(
                            tuple(category1.getId(), 5L),
                            tuple(category2.getId(), 4L),
                            tuple(category3.getId(), 3L),
                            tuple(category4.getId(), 2L),
                            tuple(category5.getId(), 1L));
        }

        @Test
        void 옷장_아이템이_5개_초과일_때_탑5만_반환한다() {
            // given
            Member member = memberUtil.getCurrentMember();
            Category category1 = categoryRepository.findById(1L).orElseThrow();
            Category category2 = categoryRepository.findById(2L).orElseThrow();
            Category category3 = categoryRepository.findById(3L).orElseThrow();

            Category category4 = Category.createCategory("testCategoryName4", null);
            Category category5 = Category.createCategory("testCategoryName5", null);
            Category category6 = Category.createCategory("testCategoryName6", null);
            categoryRepository.saveAll(List.of(category4, category5, category6));

            List<Cloth> clothes =
                    List.of(
                            // 카테고리 1 6개
                            Cloth.createCloth(
                                    "testImageUrl1",
                                    null,
                                    "testName1",
                                    "testBrand1",
                                    List.of(Season.SPRING),
                                    category1,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl2",
                                    null,
                                    "testName2",
                                    "testBrand2",
                                    List.of(Season.SPRING),
                                    category1,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl3",
                                    null,
                                    "testName3",
                                    "testBrand3",
                                    List.of(Season.SPRING),
                                    category1,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl4",
                                    null,
                                    "testName4",
                                    "testBrand4",
                                    List.of(Season.SPRING),
                                    category1,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl5",
                                    null,
                                    "testName5",
                                    "testBrand5",
                                    List.of(Season.SPRING),
                                    category1,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl6",
                                    null,
                                    "testName6",
                                    "testBrand6",
                                    List.of(Season.SPRING),
                                    category1,
                                    member),
                            // 카테고리 2 5개
                            Cloth.createCloth(
                                    "testImageUrl7",
                                    null,
                                    "testName7",
                                    "testBrand7",
                                    List.of(Season.SPRING),
                                    category2,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl8",
                                    null,
                                    "testName8",
                                    "testBrand8",
                                    List.of(Season.SPRING),
                                    category2,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl9",
                                    null,
                                    "testName9",
                                    "testBrand9",
                                    List.of(Season.SPRING),
                                    category2,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl10",
                                    null,
                                    "testName10",
                                    "testBrand10",
                                    List.of(Season.SPRING),
                                    category2,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl11",
                                    null,
                                    "testName11",
                                    "testBrand11",
                                    List.of(Season.SPRING),
                                    category2,
                                    member),
                            // 카테고리 3 4개
                            Cloth.createCloth(
                                    "testImageUrl12",
                                    null,
                                    "testName12",
                                    "testBrand12",
                                    List.of(Season.SPRING),
                                    category3,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl13",
                                    null,
                                    "testName13",
                                    "testBrand13",
                                    List.of(Season.SPRING),
                                    category3,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl14",
                                    null,
                                    "testName14",
                                    "testBrand14",
                                    List.of(Season.SPRING),
                                    category3,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl15",
                                    null,
                                    "testName15",
                                    "testBrand15",
                                    List.of(Season.SPRING),
                                    category3,
                                    member),
                            // 카테고리 4 3개
                            Cloth.createCloth(
                                    "testImageUrl16",
                                    null,
                                    "testName16",
                                    "testBrand16",
                                    List.of(Season.SPRING),
                                    category4,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl17",
                                    null,
                                    "testName17",
                                    "testBrand17",
                                    List.of(Season.SPRING),
                                    category4,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl18",
                                    null,
                                    "testName18",
                                    "testBrand18",
                                    List.of(Season.SPRING),
                                    category4,
                                    member),
                            // 카테고리 5 2개
                            Cloth.createCloth(
                                    "testImageUrl19",
                                    null,
                                    "testName19",
                                    "testBrand19",
                                    List.of(Season.SPRING),
                                    category5,
                                    member),
                            Cloth.createCloth(
                                    "testImageUrl20",
                                    null,
                                    "testName20",
                                    "testBrand20",
                                    List.of(Season.SPRING),
                                    category5,
                                    member),
                            // 카테고리 6 1개
                            Cloth.createCloth(
                                    "testImageUrl21",
                                    null,
                                    "testName21",
                                    "testBrand21",
                                    List.of(Season.SPRING),
                                    category6,
                                    member));
            clothRepository.saveAll(clothes);

            // when
            FavoriteItemsResponse response = statisticsService.getFavoriteItems();

            // then
            assertThat(response.payloads()).hasSize(5);
            assertThat(response.payloads())
                    .extracting("categoryId", "clothCount")
                    .containsExactly(
                            tuple(category1.getId(), 6L),
                            tuple(category2.getId(), 5L),
                            tuple(category3.getId(), 4L),
                            tuple(category4.getId(), 3L),
                            tuple(category5.getId(), 2L));
        }

        @Test
        void 옷장_아이템이_없을_때_빈_리스트를_반환한다() {
            // given
            Member member = memberUtil.getCurrentMember();

            // when
            FavoriteItemsResponse response = statisticsService.getFavoriteItems();

            // then
            assertThat(response.payloads()).isEmpty();
        }
    }

    @Nested
    class 옷장_활용도를_조회할_때 {

        @BeforeEach
        void setUp() {
            Member member =
                    Member.createMember(
                            "testEmail",
                            "testClokeyId",
                            "testNickName",
                            OauthInfo.createOauthInfo("testOauthId", OauthProvider.KAKAO));
            memberRepository.save(member);

            Category category = Category.createCategory("testCategoryName", null);
            categoryRepository.save(category);

            Situation situation = Situation.createSituation("testSituationName");
            situationRepository.save(situation);

            given(memberUtil.getCurrentMember()).willReturn(member);
        }

        @Test
        void 유효한_요청이면_옷장_활용도를_반환한다() {
            // given
            Member member = memberUtil.getCurrentMember();
            Category category = categoryRepository.findById(1L).orElseThrow();
            Situation situation = situationRepository.findById(1L).orElseThrow();

            Cloth utilizedCloth1 =
                    Cloth.createCloth(
                            "testImageUrl1",
                            null,
                            "testName1",
                            "testBrand1",
                            List.of(Season.SPRING),
                            category,
                            member);
            Cloth utilizedCloth2 =
                    Cloth.createCloth(
                            "testImageUrl2",
                            null,
                            "testName2",
                            "testBrand2",
                            List.of(Season.SPRING),
                            category,
                            member);
            Cloth unutilizedCloth =
                    Cloth.createCloth(
                            "testImageUrl3",
                            null,
                            "testName3",
                            "testBrand3",
                            List.of(Season.SPRING),
                            category,
                            member);

            clothRepository.saveAll(List.of(utilizedCloth1, utilizedCloth2, unutilizedCloth));

            History history = History.createHistory(LocalDate.now(), "testMemo", member, situation);
            historyRepository.save(history);

            HistoryImage historyImage = HistoryImage.createHistoryImage("testImageUrl", history);
            historyImageRepository.save(historyImage);

            HistoryClothTag historyClothTag =
                    HistoryClothTag.createHistoryClothTag(historyImage, utilizedCloth1, 0.5, 0.5);
            historyClothTagRepository.save(historyClothTag);

            Coordinate coordinate =
                    Coordinate.createDailyCoordinate("testCoordinateImageUrl", member);
            coordinateRepository.save(coordinate);

            CoordinateCloth coordinateCloth =
                    CoordinateCloth.createCoordinateCloth(
                            1.0, 1.0, 1.0, 30.0, 1, coordinate, utilizedCloth2);
            coordinateClothRepository.save(coordinateCloth);

            // when
            ClosetUtilizationResponse response =
                    statisticsService.getClosetUtilization(Season.SPRING);

            // then
            assertThat(response.utilizedCount()).isEqualTo(2);
            assertThat(response.unutilizedCount()).isEqualTo(1);
            assertThat(response.utilizedClothes()).hasSize(2);
            assertThat(response.unutilizedClothes()).hasSize(1);
            assertThat(response.utilizedClothes())
                    .extracting("imageUrl", "name", "brand")
                    .containsExactlyInAnyOrder(
                            tuple("testImageUrl1", "testName1", "testBrand1"),
                            tuple("testImageUrl2", "testName2", "testBrand2"));
            assertThat(response.unutilizedClothes())
                    .extracting("imageUrl", "name", "brand")
                    .containsExactly(tuple("testImageUrl3", "testName3", "testBrand3"));
        }

        @Test
        void 활용된_옷이_없을_때_모두_활용되지_않은_옷으로_반환한다() {
            // given
            Member member = memberUtil.getCurrentMember();
            Category category = categoryRepository.findById(1L).orElseThrow();

            Cloth unutilizedCloth1 =
                    Cloth.createCloth(
                            "testImageUrl1",
                            null,
                            "testName1",
                            "testBrand1",
                            List.of(Season.SPRING),
                            category,
                            member);
            Cloth unutilizedCloth2 =
                    Cloth.createCloth(
                            "testImageUrl2",
                            null,
                            "testName2",
                            "testBrand2",
                            List.of(Season.SPRING),
                            category,
                            member);

            clothRepository.saveAll(List.of(unutilizedCloth1, unutilizedCloth2));

            // when
            ClosetUtilizationResponse response =
                    statisticsService.getClosetUtilization(Season.SPRING);

            // then
            assertThat(response.utilizedCount()).isEqualTo(0);
            assertThat(response.unutilizedCount()).isEqualTo(2);
            assertThat(response.utilizedClothes()).isEmpty();
            assertThat(response.unutilizedClothes()).hasSize(2);
        }

        @Test
        void 모든_옷이_활용되었을_때_활용되지_않은_옷은_없다() {
            // given
            Member member = memberUtil.getCurrentMember();
            Category category = categoryRepository.findById(1L).orElseThrow();
            Situation situation = situationRepository.findById(1L).orElseThrow();

            Cloth utilizedCloth1 =
                    Cloth.createCloth(
                            "testImageUrl1",
                            null,
                            "testName1",
                            "testBrand1",
                            List.of(Season.SPRING),
                            category,
                            member);
            Cloth utilizedCloth2 =
                    Cloth.createCloth(
                            "testImageUrl2",
                            null,
                            "testName2",
                            "testBrand2",
                            List.of(Season.SPRING),
                            category,
                            member);

            clothRepository.saveAll(List.of(utilizedCloth1, utilizedCloth2));

            History history = History.createHistory(LocalDate.now(), "testMemo", member, situation);
            historyRepository.save(history);

            HistoryImage historyImage = HistoryImage.createHistoryImage("testImageUrl", history);
            historyImageRepository.save(historyImage);

            HistoryClothTag historyClothTag1 =
                    HistoryClothTag.createHistoryClothTag(historyImage, utilizedCloth1, 0.5, 0.5);
            HistoryClothTag historyClothTag2 =
                    HistoryClothTag.createHistoryClothTag(historyImage, utilizedCloth2, 0.6, 0.6);
            historyClothTagRepository.saveAll(List.of(historyClothTag1, historyClothTag2));

            // when
            ClosetUtilizationResponse response =
                    statisticsService.getClosetUtilization(Season.SPRING);

            // then
            assertThat(response.utilizedCount()).isEqualTo(2);
            assertThat(response.unutilizedCount()).isEqualTo(0);
            assertThat(response.utilizedClothes()).hasSize(2);
            assertThat(response.unutilizedClothes()).isEmpty();
        }

        @Test
        void 옷이_없을_때_모두_0으로_반환한다() {
            // given
            Member member = memberUtil.getCurrentMember();

            // when
            ClosetUtilizationResponse response =
                    statisticsService.getClosetUtilization(Season.SPRING);

            // then
            assertThat(response.utilizedCount()).isEqualTo(0);
            assertThat(response.unutilizedCount()).isEqualTo(0);
            assertThat(response.utilizedClothes()).isEmpty();
            assertThat(response.unutilizedClothes()).isEmpty();
        }

        @Test
        void DAILY_타입_Coordinate만_활용된_옷으로_카운트한다() {
            // given
            Member member = memberUtil.getCurrentMember();
            Category category = categoryRepository.findById(1L).orElseThrow();

            Cloth dailyUtilizedCloth =
                    Cloth.createCloth(
                            "testImageUrl1",
                            null,
                            "testName1",
                            "testBrand1",
                            List.of(Season.SPRING),
                            category,
                            member);
            Cloth defaultUnutilizedCloth =
                    Cloth.createCloth(
                            "testImageUrl2",
                            null,
                            "testName2",
                            "testBrand2",
                            List.of(Season.SPRING),
                            category,
                            member);
            Cloth unutilizedCloth =
                    Cloth.createCloth(
                            "testImageUrl3",
                            null,
                            "testName3",
                            "testBrand3",
                            List.of(Season.SPRING),
                            category,
                            member);

            clothRepository.saveAll(
                    List.of(dailyUtilizedCloth, defaultUnutilizedCloth, unutilizedCloth));

            Coordinate dailyCoordinate =
                    Coordinate.createDailyCoordinate("testDailyImageUrl", member);
            coordinateRepository.save(dailyCoordinate);

            CoordinateCloth dailyCoordinateCloth =
                    CoordinateCloth.createCoordinateCloth(
                            1.0, 1.0, 1.0, 30.0, 1, dailyCoordinate, dailyUtilizedCloth);
            coordinateClothRepository.save(dailyCoordinateCloth);

            LookBook lookBook = LookBook.createLookBook("testLookBookName", member);
            lookBookRepository.save(lookBook);

            Coordinate defaultCoordinate =
                    Coordinate.createCoordinateManual(
                            "testName", "testMemo", "testImageUrl", member, lookBook);
            coordinateRepository.save(defaultCoordinate);

            CoordinateCloth defaultCoordinateCloth =
                    CoordinateCloth.createCoordinateCloth(
                            1.0, 1.0, 1.0, 30.0, 1, defaultCoordinate, defaultUnutilizedCloth);
            coordinateClothRepository.save(defaultCoordinateCloth);

            // when
            ClosetUtilizationResponse response =
                    statisticsService.getClosetUtilization(Season.SPRING);

            // then
            assertThat(response.utilizedCount()).isEqualTo(1);
            assertThat(response.unutilizedCount()).isEqualTo(2);
            assertThat(response.utilizedClothes()).hasSize(1);
            assertThat(response.utilizedClothes())
                    .extracting("imageUrl", "name", "brand")
                    .containsExactly(tuple("testImageUrl1", "testName1", "testBrand1"));
            assertThat(response.unutilizedClothes())
                    .extracting("imageUrl", "name", "brand")
                    .containsExactlyInAnyOrder(
                            tuple("testImageUrl2", "testName2", "testBrand2"),
                            tuple("testImageUrl3", "testName3", "testBrand3"));
        }

        @Test
        void HistoryClothTag에_태그된_옷만_활용된_옷으로_카운트한다() {
            // given
            Member member = memberUtil.getCurrentMember();
            Category category = categoryRepository.findById(1L).orElseThrow();
            Situation situation = situationRepository.findById(1L).orElseThrow();

            Cloth utilizedCloth1 =
                    Cloth.createCloth(
                            "testImageUrl1",
                            null,
                            "testName1",
                            "testBrand1",
                            List.of(Season.SPRING),
                            category,
                            member);
            Cloth utilizedCloth2 =
                    Cloth.createCloth(
                            "testImageUrl2",
                            null,
                            "testName2",
                            "testBrand2",
                            List.of(Season.SPRING),
                            category,
                            member);
            Cloth unutilizedCloth =
                    Cloth.createCloth(
                            "testImageUrl3",
                            null,
                            "testName3",
                            "testBrand3",
                            List.of(Season.SPRING),
                            category,
                            member);

            clothRepository.saveAll(List.of(utilizedCloth1, utilizedCloth2, unutilizedCloth));

            History history = History.createHistory(LocalDate.now(), "testMemo", member, situation);
            historyRepository.save(history);

            HistoryImage historyImage = HistoryImage.createHistoryImage("testImageUrl", history);
            historyImageRepository.save(historyImage);

            HistoryClothTag historyClothTag1 =
                    HistoryClothTag.createHistoryClothTag(historyImage, utilizedCloth1, 0.5, 0.5);
            HistoryClothTag historyClothTag2 =
                    HistoryClothTag.createHistoryClothTag(historyImage, utilizedCloth2, 0.6, 0.6);
            historyClothTagRepository.saveAll(List.of(historyClothTag1, historyClothTag2));

            // when
            ClosetUtilizationResponse response =
                    statisticsService.getClosetUtilization(Season.SPRING);

            // then
            assertThat(response.utilizedCount()).isEqualTo(2);
            assertThat(response.unutilizedCount()).isEqualTo(1);
            assertThat(response.utilizedClothes()).hasSize(2);
            assertThat(response.utilizedClothes())
                    .extracting("imageUrl", "name", "brand")
                    .containsExactlyInAnyOrder(
                            tuple("testImageUrl1", "testName1", "testBrand1"),
                            tuple("testImageUrl2", "testName2", "testBrand2"));
            assertThat(response.unutilizedClothes())
                    .extracting("imageUrl", "name", "brand")
                    .containsExactly(tuple("testImageUrl3", "testName3", "testBrand3"));
        }
    }
}
