package org.clokey.domain.cloth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import org.clokey.IntegrationTest;
import org.clokey.category.entity.Category;
import org.clokey.cloth.entity.Cloth;
import org.clokey.cloth.enums.Season;
import org.clokey.coordinate.entity.Coordinate;
import org.clokey.coordinate.entity.CoordinateCloth;
import org.clokey.domain.category.exception.CategoryErrorCode;
import org.clokey.domain.category.repository.CategoryRepository;
import org.clokey.domain.cloth.dto.request.ClothCreateRequest;
import org.clokey.domain.cloth.dto.request.ClothCreateRequests;
import org.clokey.domain.cloth.dto.request.ClothImagesUploadRequest;
import org.clokey.domain.cloth.dto.request.ClothUpdateRequest;
import org.clokey.domain.cloth.dto.response.ClothDetailsResponse;
import org.clokey.domain.cloth.dto.response.ClothImagesPresignedUrlResponse;
import org.clokey.domain.cloth.dto.response.ClothListResponse;
import org.clokey.domain.cloth.dto.response.ClothRecommendListResponse;
import org.clokey.domain.cloth.exception.ClothErrorCode;
import org.clokey.domain.cloth.repository.ClothRepository;
import org.clokey.domain.coordinate.repository.CoordinateClothRepository;
import org.clokey.domain.coordinate.repository.CoordinateRepository;
import org.clokey.domain.history.repository.HistoryClothTagRepository;
import org.clokey.domain.history.repository.HistoryImageRepository;
import org.clokey.domain.history.repository.HistoryRepository;
import org.clokey.domain.history.repository.SituationRepository;
import org.clokey.domain.image.event.ImageDeleteEvent;
import org.clokey.domain.lookbook.repository.LookBookRepository;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.enums.FileExtension;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.paging.SortDirection;
import org.clokey.global.util.MemberUtil;
import org.clokey.history.entity.History;
import org.clokey.history.entity.HistoryClothTag;
import org.clokey.history.entity.HistoryImage;
import org.clokey.history.entity.Situation;
import org.clokey.lookbook.entity.LookBook;
import org.clokey.member.entity.Member;
import org.clokey.member.entity.OauthInfo;
import org.clokey.member.enums.OauthProvider;
import org.clokey.response.SliceResponse;
import org.clokey.util.S3Util;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

@RecordApplicationEvents
class ClothServiceTest extends IntegrationTest {

    @Autowired private ClothService clothService;
    @Autowired private ClothAiService clothAiService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ClothRepository clothRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private LookBookRepository lookBookRepository;
    @Autowired private CoordinateRepository coordinateRepository;
    @Autowired private CoordinateClothRepository coordinateClothRepository;
    @Autowired private HistoryRepository historyRepository;
    @Autowired private HistoryClothTagRepository historyClothTagRepository;
    @Autowired private HistoryImageRepository historyImageRepository;
    @Autowired private SituationRepository situationRepository;

    @MockitoBean private MemberUtil memberUtil;
    @MockitoBean private S3Util s3Util;
    @Autowired private ApplicationEvents applicationEvents;

    @Nested
    class 옷_업로드_presigned_url를_발급할_때 {

        @BeforeEach
        void setUp() {
            Member member =
                    Member.createMember(
                            "testEmail",
                            "testNickName",
                            OauthInfo.createOauthInfo("testOauthId", OauthProvider.KAKAO));

            memberRepository.save(member);
            given(memberUtil.getCurrentMember()).willReturn(member);
        }

        /** Only Mock Test For Coverage */
        @Test
        void 유효한_요청이면_옷을_생성한다() {
            // given
            ClothImagesUploadRequest request =
                    new ClothImagesUploadRequest(
                            List.of(
                                    new ClothImagesUploadRequest.Payload(
                                            FileExtension.JPEG, "testMd5Hash1"),
                                    new ClothImagesUploadRequest.Payload(
                                            FileExtension.JPEG, "testMd5Hash2")));
            given(s3Util.createPresignedUrl(any(), anyLong(), any(), eq("testMd5Hash1")))
                    .willReturn("testUrl1");

            given(s3Util.createPresignedUrl(any(), anyLong(), any(), eq("testMd5Hash2")))
                    .willReturn("testUrl2");

            // when
            ClothImagesPresignedUrlResponse response =
                    clothAiService.getClothUploadPresignedUrls(request);

            // then
            assertThat(response.urls()).containsExactly("testUrl1", "testUrl2");
        }
    }

    @Nested
    @Transactional
    class 옷을_생성할_때 {

        @BeforeEach
        void setUp() {
            Member member =
                    Member.createMember(
                            "testEmail",
                            "testNickName",
                            OauthInfo.createOauthInfo("testOauthId", OauthProvider.KAKAO));

            memberRepository.save(member);
            given(memberUtil.getCurrentMember()).willReturn(member);

            Category parentCategory = Category.createCategory("testParentCategory", null);
            Category category = Category.createCategory("testCategory", parentCategory);
            categoryRepository.saveAll(List.of(parentCategory, category));
        }

        @Test
        void 유효한_요청이면_옷을_생성한다() {
            // given
            ClothCreateRequests request =
                    new ClothCreateRequests(
                            List.of(
                                    new ClothCreateRequest(
                                            "testClothImageUrl1",
                                            "testClothUrl1",
                                            "testName1",
                                            "testBrand1",
                                            List.of(Season.SPRING),
                                            2L),
                                    new ClothCreateRequest(
                                            "testClothImageUrl2",
                                            "testClothUrl2",
                                            "testName2",
                                            "testBrand2",
                                            List.of(Season.SUMMER),
                                            2L)));

            // when
            clothService.createClothes(request);

            // then
            Cloth cloth1 = clothRepository.findById(1L).orElseThrow();
            Assertions.assertAll(
                    () -> assertThat(cloth1.getClothImageUrl()).isEqualTo("testClothImageUrl1"),
                    () -> assertThat(cloth1.getClothUrl()).isEqualTo("testClothUrl1"),
                    () -> assertThat(cloth1.getName()).isEqualTo("testName1"),
                    () -> assertThat(cloth1.getBrand()).isEqualTo("testBrand1"),
                    () -> assertThat(cloth1.getSeasons()).containsExactlyInAnyOrder(Season.SPRING),
                    () -> assertThat(cloth1.getCategory().getId()).isEqualTo(2L),
                    () -> assertThat(cloth1.getMember().getId()).isEqualTo(1L));

            Cloth cloth2 = clothRepository.findById(2L).orElseThrow();
            Assertions.assertAll(
                    () -> assertThat(cloth2.getClothImageUrl()).isEqualTo("testClothImageUrl2"),
                    () -> assertThat(cloth2.getClothUrl()).isEqualTo("testClothUrl2"),
                    () -> assertThat(cloth2.getName()).isEqualTo("testName2"),
                    () -> assertThat(cloth2.getBrand()).isEqualTo("testBrand2"),
                    () -> assertThat(cloth2.getSeasons()).containsExactlyInAnyOrder(Season.SUMMER),
                    () -> assertThat(cloth2.getCategory().getId()).isEqualTo(2L),
                    () -> assertThat(cloth2.getMember().getId()).isEqualTo(1L));
        }

        @Test
        void 카테고리가_존재하지_않을_경우_예외가_발생한다() {
            // given
            ClothCreateRequests request =
                    new ClothCreateRequests(
                            List.of(
                                    new ClothCreateRequest(
                                            "testClothImageUrl1",
                                            "testClothUrl1",
                                            "testName1",
                                            "testBrand1",
                                            List.of(Season.SPRING),
                                            2L),
                                    new ClothCreateRequest(
                                            "testClothImageUrl2",
                                            "testClothUrl2",
                                            "testName2",
                                            "testBrand2",
                                            List.of(Season.SPRING),
                                            999L)));

            // when & then
            assertThatThrownBy(() -> clothService.createClothes(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CategoryErrorCode.CATEGORY_IN_BULK_NOT_FOUND.getMessage());
        }

        @Test
        void 상위_카테고리로_옷을_분류하는_경우_예외가_발생한다() {
            // given
            ClothCreateRequests request =
                    new ClothCreateRequests(
                            List.of(
                                    new ClothCreateRequest(
                                            "testClothImageUrl1",
                                            "testClothUrl1",
                                            "testName1",
                                            "testBrand1",
                                            List.of(Season.SPRING),
                                            2L),
                                    new ClothCreateRequest(
                                            "testClothImageUrl2",
                                            "testClothUrl2",
                                            "testName2",
                                            "testBrand2",
                                            List.of(Season.SPRING),
                                            1L)));

            // when & then
            assertThatThrownBy(() -> clothService.createClothes(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(ClothErrorCode.PARENT_CATEGORY_CLOTH.getMessage());
        }
    }

    @Nested
    @Transactional
    class 카테고리별_계절_옷을_추천할_때 {

        @BeforeEach
        void setUp() {
            Member member =
                    Member.createMember(
                            "testEmail1",
                            "testNickName1",
                            OauthInfo.createOauthInfo("testOauthId1", OauthProvider.KAKAO));
            memberRepository.save(member);
            given(memberUtil.getCurrentMember()).willReturn(member);

            Category category1 = Category.createCategory("testCategory1", null);
            Category category2 = Category.createCategory("testCategory2", null);
            categoryRepository.saveAll(List.of(category1, category2));

            Cloth cloth1 =
                    Cloth.createCloth(
                            "testImageUrl1",
                            null,
                            null,
                            null,
                            List.of(Season.SUMMER),
                            category1,
                            member);
            Cloth cloth2 =
                    Cloth.createCloth(
                            "testImageUrl2",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category1,
                            member);
            Cloth cloth3 =
                    Cloth.createCloth(
                            "testImageUrl3",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category1,
                            member);
            Cloth cloth4 =
                    Cloth.createCloth(
                            "testImageUrl4",
                            null,
                            null,
                            null,
                            List.of(Season.SUMMER),
                            category1,
                            member);
            Cloth cloth5 =
                    Cloth.createCloth(
                            "testImageUrl5",
                            null,
                            null,
                            null,
                            List.of(Season.WINTER),
                            category1,
                            member);
            Cloth cloth6 =
                    Cloth.createCloth(
                            "testImageUrl6",
                            null,
                            null,
                            null,
                            List.of(Season.FALL),
                            category1,
                            member);

            clothRepository.saveAll(List.of(cloth1, cloth2, cloth3, cloth4, cloth5, cloth6));
        }

        @Test
        void 유효한_요청이면_가까운_계절순으로_옷을_반환한다() {
            // when
            SliceResponse<ClothRecommendListResponse> response =
                    clothService.recommendCategoryClothes(null, 6, 1L, Season.SPRING);

            // then
            assertThat(response.content())
                    .extracting("clothId")
                    .containsExactly(2L, 3L, 1L, 4L, 5L, 6L);
        }

        @Test
        void lastClothId를_입력하면_다음_Cloth_부터_조회한다() {
            // when
            SliceResponse<ClothRecommendListResponse> response =
                    clothService.recommendCategoryClothes(3L, 4, 1L, Season.SPRING);

            // then
            assertThat(response.content()).extracting("clothId").containsExactly(1L, 4L, 5L, 6L);
        }

        @Test
        void 옷을_모두_조회한_경우_빈_리스트를_반환한다() {
            // when
            SliceResponse<ClothRecommendListResponse> response =
                    clothService.recommendCategoryClothes(6L, 4, 1L, Season.SPRING);

            // then
            assertThat(response.content()).isEmpty();
        }

        @Test
        void 카테고리_옷이_존재하지_않는_경우_빈_리스트를_반환한다() {
            // when
            SliceResponse<ClothRecommendListResponse> response =
                    clothService.recommendCategoryClothes(null, 4, 2L, Season.SPRING);

            // then
            assertThat(response.content()).isEmpty();
        }

        @Test
        void 마지막_페이지인_경우_isLast를_true로_반환한다() {
            // when
            SliceResponse<ClothRecommendListResponse> response =
                    clothService.recommendCategoryClothes(null, 6, 1L, Season.SPRING);

            // then
            assertThat(response.isLast()).isTrue();
        }

        @Test
        void 마지막_페이지가_아닌_경우_isLast를_false로_반환한다() {
            // when
            SliceResponse<ClothRecommendListResponse> response =
                    clothService.recommendCategoryClothes(null, 5, 1L, Season.SPRING);

            // then
            assertThat(response.isLast()).isFalse();
        }
    }

    @Nested
    class 옷_목록을_조회할_때 {

        @BeforeEach
        void setUp() {
            clothRepository.deleteAllInBatch();

            Member member =
                    Member.createMember(
                            "testEmail1",
                            "testNickName1",
                            OauthInfo.createOauthInfo("testOauthId1", OauthProvider.KAKAO));
            memberRepository.save(member);
            given(memberUtil.getCurrentMember()).willReturn(member);

            Category category1 = Category.createCategory("testCategory1", null);
            Category category2 = Category.createCategory("testCategory2", category1);
            Category category3 = Category.createCategory("testCategory3", category1);
            categoryRepository.saveAll(List.of(category1, category2, category3));

            Cloth cloth1 =
                    Cloth.createCloth(
                            "testImageUrl1",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category2,
                            member);
            Cloth cloth2 =
                    Cloth.createCloth(
                            "testImageUrl2",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category2,
                            member);
            Cloth cloth3 =
                    Cloth.createCloth(
                            "testImageUrl3",
                            null,
                            null,
                            null,
                            List.of(Season.SUMMER),
                            category2,
                            member);
            Cloth cloth4 =
                    Cloth.createCloth(
                            "testImageUrl4",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category3,
                            member);
            Cloth cloth5 =
                    Cloth.createCloth(
                            "testImageUrl5",
                            null,
                            null,
                            null,
                            List.of(Season.SUMMER),
                            category3,
                            member);
            Cloth cloth6 =
                    Cloth.createCloth(
                            "testImageUrl6",
                            null,
                            null,
                            null,
                            List.of(Season.FALL),
                            category3,
                            member);
            clothRepository.saveAll(List.of(cloth1, cloth2, cloth3, cloth4, cloth5, cloth6));
        }

        @ParameterizedTest(name = "기본_조건_정렬_테스트 – direction={0}")
        @MethodSource("sortDirectionCases")
        void 기본_조건_정렬_테스트(SortDirection direction, List<Long> expected) {
            // when
            SliceResponse<ClothListResponse> response =
                    clothService.getClothes(null, 6, direction, null, null);

            // then
            assertThat(response.content())
                    .extracting("clothId")
                    .containsExactlyElementsOf(expected);
        }

        private static Stream<Arguments> sortDirectionCases() {
            return Stream.of(
                    Arguments.of(SortDirection.ASC, List.of(1L, 2L, 3L, 4L, 5L, 6L)),
                    Arguments.of(SortDirection.DESC, List.of(6L, 5L, 4L, 3L, 2L, 1L)));
        }

        @ParameterizedTest(name = "기본_조건_isLast_테스트 – size={0}")
        @CsvSource({"5, false", "6, true"})
        void 기본_조건_isLast_테스트(int size, boolean expectedIsLast) {
            // when
            SliceResponse<ClothListResponse> response =
                    clothService.getClothes(null, size, SortDirection.ASC, null, null);

            // then
            assertThat(response.isLast()).isEqualTo(expectedIsLast);
        }

        @Test
        void lastClothId를_입력하면_다음_cloth_부터_조회한다() {
            // when
            SliceResponse<ClothListResponse> response =
                    clothService.getClothes(2L, 4, SortDirection.ASC, null, null);

            // then
            assertThat(response.content()).extracting("clothId").containsExactly(3L, 4L, 5L, 6L);
        }

        @Test
        void 상위_카테고리_ID를_입력한_경우_하위_카테고리_옷들을_모두_조회한다() {
            // when
            SliceResponse<ClothListResponse> response =
                    clothService.getClothes(null, 6, SortDirection.ASC, 1L, null);

            // then
            assertThat(response.content())
                    .extracting("clothId")
                    .containsExactly(1L, 2L, 3L, 4L, 5L, 6L);
        }

        @Test
        void 하위_카테고리_ID를_입력한_해당_카테고리_옷들을_모두_조회한다() {
            // when
            SliceResponse<ClothListResponse> response =
                    clothService.getClothes(null, 3, SortDirection.ASC, 2L, null);

            // then
            assertThat(response.content()).extracting("clothId").containsExactly(1L, 2L, 3L);
        }

        @ParameterizedTest(name = "계절_필터_테스트 – seasons={0}")
        @MethodSource("seasonFilterCases")
        void 계절을_넣는_경우_해당_계절의_옷을_모두_조회한다(List<Season> seasons, List<Long> expected) {
            // when
            SliceResponse<ClothListResponse> response =
                    clothService.getClothes(null, 10, SortDirection.ASC, null, seasons);

            // then
            assertThat(response.content())
                    .extracting("clothId")
                    .containsExactlyElementsOf(expected);
        }

        private static Stream<Arguments> seasonFilterCases() {
            return Stream.of(
                    Arguments.of(List.of(Season.SPRING), List.of(1L, 2L, 4L)),
                    Arguments.of(List.of(Season.SUMMER), List.of(3L, 5L)),
                    Arguments.of(List.of(Season.FALL), List.of(6L)),
                    Arguments.of(
                            List.of(Season.SPRING, Season.SUMMER), List.of(1L, 2L, 3L, 4L, 5L)));
        }

        @Test
        void 복합_조건_테스트() {
            // when
            SliceResponse<ClothListResponse> response =
                    clothService.getClothes(2L, 4, SortDirection.ASC, 3L, List.of(Season.SPRING));

            // then
            assertThat(response.content()).extracting("clothId").containsExactly(4L);
        }

        @Test
        void 카테고리가_존재하지_않는_경우_예외가_발생한다() {
            // when & then
            assertThatThrownBy(
                            () -> clothService.getClothes(null, 6, SortDirection.ASC, 999L, null))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CategoryErrorCode.CATEGORY_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 옷을_상세_조회할_때 {

        @BeforeEach
        void setUp() {
            clothRepository.deleteAllInBatch();

            Member member1 =
                    Member.createMember(
                            "testEmail1",
                            "testNickName1",
                            OauthInfo.createOauthInfo("testOauthId1", OauthProvider.KAKAO));
            Member member2 =
                    Member.createMember(
                            "testEmail2",
                            "testNickName2",
                            OauthInfo.createOauthInfo("testOauthId2", OauthProvider.KAKAO));
            memberRepository.saveAll(List.of(member1, member2));
            given(memberUtil.getCurrentMember()).willReturn(member1);

            Category parentCategory = Category.createCategory("testParentCategory", null);
            Category category = Category.createCategory("testCategory", parentCategory);
            categoryRepository.saveAll(List.of(parentCategory, category));

            Cloth cloth1 =
                    Cloth.createCloth(
                            "testImageUrl1",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member1);
            Cloth cloth2 =
                    Cloth.createCloth(
                            "testImageUrl2",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member2);
            clothRepository.saveAll(List.of(cloth1, cloth2));
        }

        @Test
        void 유효한_요청이면_옷_상세_정보를_반환한다() {
            // when
            ClothDetailsResponse response = clothService.getClothDetails(1L);

            // then
            assertThat(response)
                    .extracting(
                            "clothImageUrl",
                            "parentCategory",
                            "category",
                            "name",
                            "brand",
                            "clothUrl",
                            "seasons")
                    .containsExactly(
                            "testImageUrl1",
                            "testParentCategory",
                            "testCategory",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING));
        }

        @Test
        void 존재하지_않는_categoryId를_입력하면_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> clothService.getClothDetails(999L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(ClothErrorCode.ClOTH_NOT_FOUND.getMessage());
        }

        @Test
        void 옷의_소유자가_아닌_경우_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> clothService.getClothDetails(2L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(ClothErrorCode.NOT_CLOTH_OWNER.getMessage());
        }
    }

    @Nested
    class 옷을_수정할_때 {

        @BeforeEach
        void setUp() {
            Member member1 =
                    Member.createMember(
                            "testEmail1",
                            "testNickName1",
                            OauthInfo.createOauthInfo("testOauthId1", OauthProvider.KAKAO));
            Member member2 =
                    Member.createMember(
                            "testEmail2",
                            "testNickName2",
                            OauthInfo.createOauthInfo("testOauthId2", OauthProvider.KAKAO));
            memberRepository.saveAll(List.of(member1, member2));
            given(memberUtil.getCurrentMember()).willReturn(member1);

            Category parentCategory = Category.createCategory("testParentCategory", null);
            Category category = Category.createCategory("testCategory", parentCategory);
            categoryRepository.saveAll(List.of(parentCategory, category));

            Cloth cloth1 =
                    Cloth.createCloth(
                            "testImageUrl1",
                            "testClothUrl",
                            "testName",
                            "testBrand",
                            List.of(Season.SPRING),
                            category,
                            member1);
            Cloth cloth2 =
                    Cloth.createCloth(
                            "testImageUrl2",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member2);
            clothRepository.saveAll(List.of(cloth1, cloth2));
        }

        @Test
        @Transactional
        void 유효한_요청이면_옷을_수정한다() {
            // given
            ClothUpdateRequest request =
                    new ClothUpdateRequest(
                            "newClothImageUrl",
                            "newClothUrl",
                            "newName",
                            "newBrand",
                            List.of(Season.SUMMER),
                            2L);

            // when
            clothService.updateCloth(1L, request);

            Cloth updatedCloth = clothRepository.findById(1L).orElseThrow();
            Assertions.assertAll(
                    () -> assertThat(updatedCloth.getClothImageUrl()).isEqualTo("newClothImageUrl"),
                    () -> assertThat(updatedCloth.getClothUrl()).isEqualTo("newClothUrl"),
                    () -> assertThat(updatedCloth.getName()).isEqualTo("newName"),
                    () -> assertThat(updatedCloth.getBrand()).isEqualTo("newBrand"),
                    () -> assertThat(updatedCloth.getCategory().getId()).isEqualTo(2L),
                    () -> assertThat(updatedCloth.getMember().getId()).isEqualTo(1L),
                    () -> assertThat(updatedCloth.getSeasons()).containsExactly(Season.SUMMER));
        }

        @Test
        void 옷의_이미지_url이_수정되는_경우_기존_url을_삭제하는_이벤트를_발행한다() {
            // given
            ClothUpdateRequest request =
                    new ClothUpdateRequest(
                            "newClothImageUrl",
                            "newClothUrl",
                            "newName",
                            "newBrand",
                            List.of(Season.SUMMER),
                            2L);

            // when
            clothService.updateCloth(1L, request);

            var events = applicationEvents.stream(ImageDeleteEvent.class).toList();
            assertThat(events).hasSize(1);
            assertThat(events.getFirst().imageUrl()).isEqualTo("testImageUrl1");
        }

        @Test
        void 옷이_존재하지_않으면_예외가_발생한다() {
            // given
            ClothUpdateRequest request =
                    new ClothUpdateRequest(
                            "newClothImageUrl",
                            "newClothUrl",
                            "newName",
                            "newBrand",
                            List.of(Season.SUMMER),
                            2L);

            // when & then
            assertThatThrownBy(() -> clothService.updateCloth(999L, request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(ClothErrorCode.ClOTH_NOT_FOUND.getMessage());
        }

        @Test
        void 카테고리가_존재하지_않으면_예외가_발생한다() {
            // given
            ClothUpdateRequest request =
                    new ClothUpdateRequest(
                            "newClothImageUrl",
                            "newClothUrl",
                            "newName",
                            "newBrand",
                            List.of(Season.SUMMER),
                            999L);

            // when & then
            assertThatThrownBy(() -> clothService.updateCloth(1L, request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CategoryErrorCode.CATEGORY_NOT_FOUND.getMessage());
        }

        @Test
        void 나의_옷이_아닌_경우_예외가_발생한다() {
            // given
            ClothUpdateRequest request =
                    new ClothUpdateRequest(
                            "newClothImageUrl",
                            "newClothUrl",
                            "newName",
                            "newBrand",
                            List.of(Season.SUMMER),
                            2L);

            // when & then
            assertThatThrownBy(() -> clothService.updateCloth(2L, request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(ClothErrorCode.NOT_CLOTH_OWNER.getMessage());
        }

        @Test
        void 부모_카테고리를_옷에_등록하려는_경우_예외가_발생한다() {
            // given
            ClothUpdateRequest request =
                    new ClothUpdateRequest(
                            "newClothImageUrl",
                            "newClothUrl",
                            "newName",
                            "newBrand",
                            List.of(Season.SUMMER),
                            1L);

            // when & then
            assertThatThrownBy(() -> clothService.updateCloth(1L, request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(ClothErrorCode.PARENT_CATEGORY_CLOTH.getMessage());
        }
    }

    @Nested
    class 옷을_삭제할_때 {

        @BeforeEach
        void setUp() {
            Member member1 =
                    Member.createMember(
                            "testEmail1",
                            "testNickName1",
                            OauthInfo.createOauthInfo("testOauthId", OauthProvider.KAKAO));
            Member member2 =
                    Member.createMember(
                            "testEmail2",
                            "testNickName2",
                            OauthInfo.createOauthInfo("testOauthId", OauthProvider.KAKAO));

            memberRepository.saveAll(List.of(member1, member2));
            given(memberUtil.getCurrentMember()).willReturn(member1);

            Category parentCategory = Category.createCategory("testParentCategory", null);
            Category category = Category.createCategory("testCategory", parentCategory);
            categoryRepository.saveAll(List.of(parentCategory, category));

            Cloth cloth1 =
                    Cloth.createCloth(
                            "testImageUrl1",
                            "testClothUrl",
                            "testName",
                            "testBrand",
                            List.of(Season.SPRING),
                            category,
                            member1);
            Cloth cloth2 =
                    Cloth.createCloth(
                            "testImageUrl2",
                            "testClothUrl",
                            "testName",
                            "testBrand",
                            List.of(Season.SPRING),
                            category,
                            member2);
            clothRepository.saveAll(List.of(cloth1, cloth2));

            // coordinate 관련 매핑 테이블 세팅
            LookBook lookBook = LookBook.createLookBook("testName", member1);
            lookBookRepository.save(lookBook);

            Coordinate coordinate =
                    Coordinate.createCoordinateManual(
                            "testName", "testMemo", "testImageUrl", member1, lookBook);
            coordinateRepository.save(coordinate);

            CoordinateCloth coordinateCloth =
                    CoordinateCloth.createCoordinateCloth(
                            1.0, 1.0, 1.0, 30.0, 1, coordinate, cloth1);
            coordinateClothRepository.save(coordinateCloth);

            // history 관련 매핑 테이블 세팅
            Situation situation = Situation.createSituation("testName");
            situationRepository.save(situation);

            History history =
                    History.createHistory(
                            LocalDate.of(2025, 1, 1), "testContent", member1, situation);
            historyRepository.save(history);

            HistoryImage historyImage = HistoryImage.createHistoryImage("testImageUrl", history);
            historyImageRepository.save(historyImage);

            HistoryClothTag historyClothTag =
                    HistoryClothTag.createHistoryClothTag(historyImage, cloth1, 1.0, 1.0);
            historyClothTagRepository.save(historyClothTag);
        }

        @Test
        void 유효한_요청이면_옷을_삭제하고_관련_매핑테이블을_모두_삭제하고_옷의_이미지를_삭제하는_이벤트를_발행한다() {
            // when
            clothService.deleteCloth(1L);

            // then
            var events = applicationEvents.stream(ImageDeleteEvent.class).toList();

            Assertions.assertAll(
                    () -> assertThat(events).hasSize(1),
                    () -> assertThat(events.getFirst().imageUrl()).isEqualTo("testImageUrl1"),
                    () -> assertThat(coordinateClothRepository.findById(1L).isPresent()).isFalse(),
                    () -> assertThat(historyClothTagRepository.findById(1L).isPresent()).isFalse());
        }

        @Test
        void 옷이_존재하지_않는_경우_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> clothService.deleteCloth(999L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(ClothErrorCode.ClOTH_NOT_FOUND.getMessage());
        }

        @Test
        void 나의_옷이_아닌_경우_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> clothService.deleteCloth(2L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(ClothErrorCode.NOT_CLOTH_OWNER.getMessage());
        }
    }
}
