package org.clokey.domain.coordinate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.clokey.IntegrationTest;
import org.clokey.RedisCleaner;
import org.clokey.TransactionUtil;
import org.clokey.category.entity.Category;
import org.clokey.cloth.entity.Cloth;
import org.clokey.cloth.enums.Season;
import org.clokey.coordinate.entity.Coordinate;
import org.clokey.coordinate.entity.CoordinateCloth;
import org.clokey.coordinate.enums.CoordinateType;
import org.clokey.domain.category.repository.CategoryRepository;
import org.clokey.domain.cloth.exception.ClothErrorCode;
import org.clokey.domain.cloth.repository.ClothRepository;
import org.clokey.domain.coordinate.dto.request.CoordinateAutoCreateRequest;
import org.clokey.domain.coordinate.dto.request.CoordinateManualCreateRequest;
import org.clokey.domain.coordinate.dto.request.CoordinateUpdateRequest;
import org.clokey.domain.coordinate.dto.request.DailyCoordinateCreateRequest;
import org.clokey.domain.coordinate.dto.response.*;
import org.clokey.domain.coordinate.exception.CoordinateErrorCode;
import org.clokey.domain.coordinate.repository.CoordinateClothRepository;
import org.clokey.domain.coordinate.repository.CoordinateRepository;
import org.clokey.domain.image.event.ImageDeleteEvent;
import org.clokey.domain.lookbook.exception.LookBookErrorCode;
import org.clokey.domain.lookbook.repository.LookBookRepository;
import org.clokey.domain.member.exception.MemberErrorCode;
import org.clokey.domain.member.repository.BlockRepository;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.paging.SortDirection;
import org.clokey.global.util.MemberUtil;
import org.clokey.lookbook.entity.LookBook;
import org.clokey.member.entity.Block;
import org.clokey.member.entity.Member;
import org.clokey.member.entity.OauthInfo;
import org.clokey.member.enums.OauthProvider;
import org.clokey.response.SliceResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

@RecordApplicationEvents
class CoordinateServiceImplTest extends IntegrationTest {

    @Autowired private TransactionUtil transactionUtil;
    @Autowired private RedisCleaner redisCleaner;
    @Autowired private ApplicationEvents applicationEvents;

    @Autowired private CoordinateRepository coordinateRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private BlockRepository blockRepository;
    @Autowired private CoordinateClothRepository coordinateClothRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ClothRepository clothRepository;
    @Autowired private CoordinateService coordinateService;
    @Autowired private LookBookRepository lookBookRepository;
    @Autowired private StringRedisTemplate redisTemplate;

    @MockitoBean private MemberUtil memberUtil;

    @Nested
    class 오늘의_코디를_생성할_때 {

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

            Category category = Category.createCategory("testCategory", null);
            categoryRepository.save(category);

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
                            member1);
            Cloth cloth3 =
                    Cloth.createCloth(
                            "testImageUrl3",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member2);

            Cloth cloth4 =
                    Cloth.createCloth(
                            "testImageUrl4",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member1);
            Cloth cloth5 =
                    Cloth.createCloth(
                            "testImageUrl5",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member1);
            Cloth cloth6 =
                    Cloth.createCloth(
                            "testImageUrl6",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member1);
            Cloth cloth7 =
                    Cloth.createCloth(
                            "testImageUrl7",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member1);
            Cloth cloth8 =
                    Cloth.createCloth(
                            "testImageUrl8",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member1);
            Cloth cloth9 =
                    Cloth.createCloth(
                            "testImageUrl9",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member1);
            Cloth cloth10 =
                    Cloth.createCloth(
                            "testImageUrl10",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member1);
            Cloth cloth11 =
                    Cloth.createCloth(
                            "testImageUrl11",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member1);
            Cloth cloth12 =
                    Cloth.createCloth(
                            "testImageUrl12",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member1);
            clothRepository.saveAll(
                    List.of(
                            cloth1, cloth2, cloth3, cloth4, cloth5, cloth6, cloth7, cloth8, cloth9,
                            cloth10, cloth11, cloth12));
        }

        @Test
        void 유효한_요청이면_오늘의_코디를_생성한다() {
            // given
            DailyCoordinateCreateRequest request =
                    new DailyCoordinateCreateRequest(
                            "testUrl",
                            List.of(
                                    new DailyCoordinateCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 1),
                                    new DailyCoordinateCreateRequest.Payload(
                                            2L, 100.5, 200.25, 1.0, 50.0, 2)));

            // when
            coordinateService.createDailyCoordinate(request);

            // then
            Coordinate coordinate =
                    transactionUtil.getResult(
                            () -> {
                                Coordinate loadedCoordinate =
                                        coordinateRepository.findById(1L).get();
                                loadedCoordinate.getCoordinateClothes().get(0);
                                return loadedCoordinate;
                            });

            Assertions.assertAll(
                    () ->
                            assertThat(coordinate)
                                    .extracting("imageUrl", "member.id", "coordinateType")
                                    .containsExactly("testUrl", 1L, CoordinateType.DAILY),
                    () ->
                            assertThat(coordinate.getCoordinateClothes())
                                    .extracting(
                                            cc -> cc.getCloth().getId(),
                                            CoordinateCloth::getOrder,
                                            CoordinateCloth::getRatio)
                                    .containsExactlyInAnyOrder(
                                            tuple(1L, 1, 1.0), tuple(2L, 2, 1.0)));
        }

        @Test
        void 옷의_ORDER가_유효하지_않은_경우_에외가_발생한다() {
            // given
            DailyCoordinateCreateRequest request =
                    new DailyCoordinateCreateRequest(
                            "testUrl",
                            List.of(
                                    new DailyCoordinateCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 1),
                                    new DailyCoordinateCreateRequest.Payload(
                                            2L, 100.5, 200.25, 1.0, 50.0, 3)));

            // when & then
            assertThatThrownBy(() -> coordinateService.createDailyCoordinate(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CoordinateErrorCode.INVALID_ORDER.getMessage());
        }

        @Test
        void 옷을_10개_초과로_등록하는_경우_예외가_발생한다() {
            // given
            DailyCoordinateCreateRequest request =
                    new DailyCoordinateCreateRequest(
                            "testUrl",
                            List.of(
                                    new DailyCoordinateCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 1),
                                    new DailyCoordinateCreateRequest.Payload(
                                            2L, 100.5, 200.25, 1.0, 50.0, 2),
                                    new DailyCoordinateCreateRequest.Payload(
                                            3L, 100.5, 200.25, 1.0, 50.0, 3),
                                    new DailyCoordinateCreateRequest.Payload(
                                            4L, 100.5, 200.25, 1.0, 50.0, 4),
                                    new DailyCoordinateCreateRequest.Payload(
                                            5L, 100.5, 200.25, 1.0, 50.0, 5),
                                    new DailyCoordinateCreateRequest.Payload(
                                            6L, 100.5, 200.25, 1.0, 50.0, 6),
                                    new DailyCoordinateCreateRequest.Payload(
                                            7L, 100.5, 200.25, 1.0, 50.0, 7),
                                    new DailyCoordinateCreateRequest.Payload(
                                            8L, 100.5, 200.25, 1.0, 50.0, 8),
                                    new DailyCoordinateCreateRequest.Payload(
                                            9L, 100.5, 200.25, 1.0, 50.0, 9),
                                    new DailyCoordinateCreateRequest.Payload(
                                            10L, 100.5, 200.25, 1.0, 50.0, 10),
                                    new DailyCoordinateCreateRequest.Payload(
                                            11L, 100.5, 200.25, 1.0, 50.0, 11)));

            // when & then
            assertThatThrownBy(() -> coordinateService.createDailyCoordinate(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CoordinateErrorCode.CLOTHES_OVER_COORDINATION_LIMIT.getMessage());
        }

        @Test
        void 중복된_옷을_입력하면_예외가_발생한다() {
            // given
            DailyCoordinateCreateRequest request =
                    new DailyCoordinateCreateRequest(
                            "testUrl",
                            List.of(
                                    new DailyCoordinateCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 1),
                                    new DailyCoordinateCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 2)));

            // when & then
            assertThatThrownBy(() -> coordinateService.createDailyCoordinate(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(ClothErrorCode.DUPLICATED_CLOTH.getMessage());
        }

        @Test
        void 존재하지_않는_옷을_포함하는_경우_예외가_발생한다() {
            // given
            DailyCoordinateCreateRequest request =
                    new DailyCoordinateCreateRequest(
                            "testUrl",
                            List.of(
                                    new DailyCoordinateCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 1),
                                    new DailyCoordinateCreateRequest.Payload(
                                            999L, 100.5, 200.25, 1.0, 50.0, 2)));

            // when & then
            assertThatThrownBy(() -> coordinateService.createDailyCoordinate(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(ClothErrorCode.ClOTH_NOT_FOUND.getMessage());
        }

        @Test
        void 나의_옷이_아닌_옷이_포함된_경우_예외가_발생한다() {
            // given
            DailyCoordinateCreateRequest request =
                    new DailyCoordinateCreateRequest(
                            "testUrl",
                            List.of(
                                    new DailyCoordinateCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 1),
                                    new DailyCoordinateCreateRequest.Payload(
                                            3L, 100.5, 200.25, 1.0, 50.0, 2)));

            // when & then
            assertThatThrownBy(() -> coordinateService.createDailyCoordinate(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(ClothErrorCode.NOT_CLOTH_OWNER.getMessage());
        }

        @Test
        void 이미_오늘의_코디가_있는_경우_예외가_발생한다() {
            // given
            DailyCoordinateCreateRequest request =
                    new DailyCoordinateCreateRequest(
                            "testUrl",
                            List.of(
                                    new DailyCoordinateCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 1),
                                    new DailyCoordinateCreateRequest.Payload(
                                            2L, 100.5, 200.25, 1.0, 50.0, 2)));

            Member member = memberRepository.findById(1L).orElseThrow();
            Coordinate coordinate = Coordinate.createDailyCoordinate("testImageUrl", member);
            coordinateRepository.save(coordinate);

            // when & then
            assertThatThrownBy(() -> coordinateService.createDailyCoordinate(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CoordinateErrorCode.DAILY_COORDINATE_ALREADY_EXISTS.getMessage());
        }
    }

    @Nested
    class 코디를_수동_생성할_때 {

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

            Category category = Category.createCategory("testCategory", null);
            categoryRepository.save(category);

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
                            member1);
            Cloth cloth3 =
                    Cloth.createCloth(
                            "testImageUrl3",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member2);

            Cloth cloth4 =
                    Cloth.createCloth(
                            "testImageUrl4",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member1);
            Cloth cloth5 =
                    Cloth.createCloth(
                            "testImageUrl5",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member1);
            Cloth cloth6 =
                    Cloth.createCloth(
                            "testImageUrl6",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member1);
            Cloth cloth7 =
                    Cloth.createCloth(
                            "testImageUrl7",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member1);
            Cloth cloth8 =
                    Cloth.createCloth(
                            "testImageUrl8",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member1);
            Cloth cloth9 =
                    Cloth.createCloth(
                            "testImageUrl9",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member1);
            Cloth cloth10 =
                    Cloth.createCloth(
                            "testImageUrl10",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member1);
            Cloth cloth11 =
                    Cloth.createCloth(
                            "testImageUrl11",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member1);
            Cloth cloth12 =
                    Cloth.createCloth(
                            "testImageUrl12",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member1);
            clothRepository.saveAll(
                    List.of(
                            cloth1, cloth2, cloth3, cloth4, cloth5, cloth6, cloth7, cloth8, cloth9,
                            cloth10, cloth11, cloth12));

            LookBook lookBook1 = LookBook.createLookBook("testName1", member1);
            LookBook lookBook2 = LookBook.createLookBook("testName2", member2);
            lookBookRepository.saveAll(List.of(lookBook1, lookBook2));
        }

        @Test
        void 유효한_요청이면_코디를_생성한다() {
            // given
            CoordinateManualCreateRequest request =
                    new CoordinateManualCreateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            1L,
                            List.of(
                                    new CoordinateManualCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 1),
                                    new CoordinateManualCreateRequest.Payload(
                                            2L, 100.5, 200.25, 1.0, 50.0, 2)));

            // when
            coordinateService.createCoordinateManual(request);

            // then
            Coordinate coordinate =
                    transactionUtil.getResult(
                            () -> {
                                Coordinate loadedCoordinate =
                                        coordinateRepository.findById(1L).get();
                                loadedCoordinate.getCoordinateClothes().get(0);
                                return loadedCoordinate;
                            });

            Assertions.assertAll(
                    () ->
                            assertThat(coordinate)
                                    .extracting(
                                            "imageUrl", "name", "memo", "lookBook.id", "member.id")
                                    .containsExactly("testUrl", "testName", "testMemo", 1L, 1L),
                    () ->
                            assertThat(coordinate.getCoordinateClothes())
                                    .extracting(
                                            cc -> cc.getCloth().getId(),
                                            CoordinateCloth::getOrder,
                                            CoordinateCloth::getRatio)
                                    .containsExactlyInAnyOrder(
                                            tuple(1L, 1, 1.0), tuple(2L, 2, 1.0)));
        }

        @Test
        void 옷의_ORDER가_유효하지_않은_경우_에외가_발생한다() {
            // given
            CoordinateManualCreateRequest request =
                    new CoordinateManualCreateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            1L,
                            List.of(
                                    new CoordinateManualCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 1),
                                    new CoordinateManualCreateRequest.Payload(
                                            2L, 100.5, 200.25, 1.0, 50.0, 3)));
            // when & then
            assertThatThrownBy(() -> coordinateService.createCoordinateManual(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CoordinateErrorCode.INVALID_ORDER.getMessage());
        }

        @Test
        void 옷을_10개_초과로_등록하는_경우_예외가_발생한다() {
            // given
            CoordinateManualCreateRequest request =
                    new CoordinateManualCreateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            1L,
                            List.of(
                                    new CoordinateManualCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 1),
                                    new CoordinateManualCreateRequest.Payload(
                                            2L, 100.5, 200.25, 1.0, 50.0, 2),
                                    new CoordinateManualCreateRequest.Payload(
                                            3L, 100.5, 200.25, 1.0, 50.0, 3),
                                    new CoordinateManualCreateRequest.Payload(
                                            4L, 100.5, 200.25, 1.0, 50.0, 4),
                                    new CoordinateManualCreateRequest.Payload(
                                            5L, 100.5, 200.25, 1.0, 50.0, 5),
                                    new CoordinateManualCreateRequest.Payload(
                                            6L, 100.5, 200.25, 1.0, 50.0, 6),
                                    new CoordinateManualCreateRequest.Payload(
                                            7L, 100.5, 200.25, 1.0, 50.0, 7),
                                    new CoordinateManualCreateRequest.Payload(
                                            8L, 100.5, 200.25, 1.0, 50.0, 8),
                                    new CoordinateManualCreateRequest.Payload(
                                            9L, 100.5, 200.25, 1.0, 50.0, 9),
                                    new CoordinateManualCreateRequest.Payload(
                                            10L, 100.5, 200.25, 1.0, 50.0, 10),
                                    new CoordinateManualCreateRequest.Payload(
                                            11L, 100.5, 200.25, 1.0, 50.0, 11)));

            // when & then
            assertThatThrownBy(() -> coordinateService.createCoordinateManual(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CoordinateErrorCode.CLOTHES_OVER_COORDINATION_LIMIT.getMessage());
        }

        @Test
        void 중복된_옷을_입력하면_예외가_발생한다() {
            // given
            CoordinateManualCreateRequest request =
                    new CoordinateManualCreateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            1L,
                            List.of(
                                    new CoordinateManualCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 1),
                                    new CoordinateManualCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 2)));

            // when & then
            assertThatThrownBy(() -> coordinateService.createCoordinateManual(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(ClothErrorCode.DUPLICATED_CLOTH.getMessage());
        }

        @Test
        void 존재하지_않는_옷을_포함하는_경우_예외가_발생한다() {
            // given
            CoordinateManualCreateRequest request =
                    new CoordinateManualCreateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            1L,
                            List.of(
                                    new CoordinateManualCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 1),
                                    new CoordinateManualCreateRequest.Payload(
                                            999L, 100.5, 200.25, 1.0, 50.0, 3)));

            // when & then
            assertThatThrownBy(() -> coordinateService.createCoordinateManual(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(ClothErrorCode.ClOTH_NOT_FOUND.getMessage());
        }

        @Test
        void 나의_옷이_아닌_옷이_포함된_경우_예외가_발생한다() {
            // given
            CoordinateManualCreateRequest request =
                    new CoordinateManualCreateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            1L,
                            List.of(
                                    new CoordinateManualCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 1),
                                    new CoordinateManualCreateRequest.Payload(
                                            3L, 100.5, 200.25, 1.0, 50.0, 2)));
            // when & then
            assertThatThrownBy(() -> coordinateService.createCoordinateManual(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(ClothErrorCode.NOT_CLOTH_OWNER.getMessage());
        }

        @Test
        void 나의_룩북이_아닌_경우_예외가_발생한다() {
            // given
            CoordinateManualCreateRequest request =
                    new CoordinateManualCreateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            2L,
                            List.of(
                                    new CoordinateManualCreateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 1),
                                    new CoordinateManualCreateRequest.Payload(
                                            2L, 100.5, 200.25, 1.0, 50.0, 2)));
            // when & then
            assertThatThrownBy(() -> coordinateService.createCoordinateManual(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(LookBookErrorCode.NOT_LOOK_BOOK_OWNER.getMessage());
        }
    }

    @Nested
    class 코디를_자동_생성할_때 {

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

            LookBook lookBook1 = LookBook.createLookBook("testName1", member1);
            LookBook lookBook2 = LookBook.createLookBook("testName2", member2);
            lookBookRepository.saveAll(List.of(lookBook1, lookBook2));

            Coordinate coordinate1 = Coordinate.createDailyCoordinate("testUrl1", member1);
            Coordinate coordinate2 =
                    Coordinate.createCoordinateManual(
                            "testCoordinate", "testMemo", "testUrl2", member1, lookBook1);
            Coordinate coordinate3 = Coordinate.createDailyCoordinate("testUrl3", member2);

            coordinateRepository.saveAll(List.of(coordinate1, coordinate2, coordinate3));
        }

        @Test
        void 유효한_요청이면_오늘의_코디를_룩북에_추가한다() {
            // given
            CoordinateAutoCreateRequest request =
                    new CoordinateAutoCreateRequest("testName", "testMemo", 1L, 1L);

            // when
            coordinateService.createCoordinateAuto(request);

            // then
            Coordinate coordinate = coordinateRepository.findById(1L).orElseThrow();

            Assertions.assertAll(
                    () ->
                            assertThat(coordinate)
                                    .extracting("name", "memo", "lookBook.id")
                                    .containsExactly("testName", "testMemo", 1L));
        }

        @Test
        void 이름이_비어있으면_날짜_기반으로_자동_생성한다() {
            // given
            Coordinate coordinate = coordinateRepository.findById(1L).orElseThrow();
            LocalDate createdAt = coordinate.getCreatedAt().toLocalDate();
            String expectedName =
                    "오늘의 코디 (" + createdAt.format(DateTimeFormatter.ofPattern("MM.dd.yy")) + ")";

            CoordinateAutoCreateRequest request =
                    new CoordinateAutoCreateRequest(null, "testMemo", 1L, 1L);

            // when
            coordinateService.createCoordinateAuto(request);

            // then
            Coordinate result = coordinateRepository.findById(1L).orElseThrow();

            Assertions.assertAll(
                    () ->
                            assertThat(result)
                                    .extracting("name", "memo", "lookBook.id")
                                    .containsExactly(expectedName, "testMemo", 1L));
        }

        @Test
        void 이름이_공백이면_날짜_기반으로_자동_생성한다() {
            // given
            Coordinate coordinate = coordinateRepository.findById(1L).orElseThrow();
            LocalDate createdAt = coordinate.getCreatedAt().toLocalDate();
            String expectedName =
                    "오늘의 코디 (" + createdAt.format(DateTimeFormatter.ofPattern("MM.dd.yy")) + ")";

            CoordinateAutoCreateRequest request =
                    new CoordinateAutoCreateRequest("   ", "testMemo", 1L, 1L);

            // when
            coordinateService.createCoordinateAuto(request);

            // then
            Coordinate result = coordinateRepository.findById(1L).orElseThrow();

            Assertions.assertAll(
                    () ->
                            assertThat(result)
                                    .extracting("name", "memo", "lookBook.id")
                                    .containsExactly(expectedName, "testMemo", 1L));
        }

        @Test
        void 나의_코디가_아닌_경우_예외가_발생한다() {
            // given
            CoordinateAutoCreateRequest request =
                    new CoordinateAutoCreateRequest("testName", "testMemo", 3L, 1L);

            // when & then
            assertThatThrownBy(() -> coordinateService.createCoordinateAuto(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CoordinateErrorCode.NOT_COORDINATE_OWNER.getMessage());
        }

        @Test
        void 나의_룩북이_아닌_경우_예외가_발생한다() {
            // given
            CoordinateAutoCreateRequest request =
                    new CoordinateAutoCreateRequest("testName", "testMemo", 1L, 2L);

            // when & then
            assertThatThrownBy(() -> coordinateService.createCoordinateAuto(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(LookBookErrorCode.NOT_LOOK_BOOK_OWNER.getMessage());
        }

        @Test
        void 오늘의_코디가_아닌_경우_예외가_발생한다() {
            // given
            CoordinateAutoCreateRequest request =
                    new CoordinateAutoCreateRequest("testName", "testMemo", 2L, 1L);

            // when & then
            assertThatThrownBy(() -> coordinateService.createCoordinateAuto(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CoordinateErrorCode.NOT_DAILY_COORDINATE.getMessage());
        }
    }

    @Nested
    class 코디를_업데이트할_때 {

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

            LookBook lookBook1 = LookBook.createLookBook("testName1", member1);
            LookBook lookBook2 = LookBook.createLookBook("testName2", member2);
            lookBookRepository.saveAll(List.of(lookBook1, lookBook2));

            Category category = Category.createCategory("testCategory", null);
            categoryRepository.save(category);

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
                            member1);
            Cloth cloth3 =
                    Cloth.createCloth(
                            "testImageUrl3",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member1);
            Cloth cloth4 =
                    Cloth.createCloth(
                            "testImageUrl4",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member2);

            Cloth cloth5 =
                    Cloth.createCloth(
                            "testImageUrl5",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member1);
            Cloth cloth6 =
                    Cloth.createCloth(
                            "testImageUrl6",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member1);
            Cloth cloth7 =
                    Cloth.createCloth(
                            "testImageUrl7",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member1);
            Cloth cloth8 =
                    Cloth.createCloth(
                            "testImageUrl8",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member1);
            Cloth cloth9 =
                    Cloth.createCloth(
                            "testImageUrl9",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member1);
            Cloth cloth10 =
                    Cloth.createCloth(
                            "testImageUrl10",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member1);
            Cloth cloth11 =
                    Cloth.createCloth(
                            "testImageUrl11",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member1);
            Cloth cloth12 =
                    Cloth.createCloth(
                            "testImageUrl12",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member1);
            clothRepository.saveAll(
                    List.of(
                            cloth1, cloth2, cloth3, cloth4, cloth5, cloth6, cloth7, cloth8, cloth9,
                            cloth10, cloth11, cloth12));

            Coordinate coordinate1 =
                    Coordinate.createCoordinateManual(
                            "testName", "testMemo", "testUrl", member1, lookBook1);
            Coordinate coordinate2 =
                    Coordinate.createCoordinateManual(
                            "testName", "testMemo", "testUrl", member2, lookBook2);
            coordinateRepository.saveAll(List.of(coordinate1, coordinate2));

            CoordinateCloth coordinateCloth1 =
                    CoordinateCloth.createCoordinateCloth(
                            1.0, 1.0, 1.0, 50.0, 1, coordinate1, cloth1);
            CoordinateCloth coordinateCloth2 =
                    CoordinateCloth.createCoordinateCloth(
                            2.0, 2.0, 2.0, 60.0, 2, coordinate1, cloth2);
            coordinateClothRepository.saveAll(List.of(coordinateCloth1, coordinateCloth2));
        }

        @Test
        void 유효한_요청이면_코디_정보가_업데이트된다() {
            // given
            CoordinateUpdateRequest request =
                    new CoordinateUpdateRequest(
                            "updatedTestUrl",
                            "updatedTestName",
                            "updatedTestMemo",
                            List.of(
                                    new CoordinateUpdateRequest.Payload(
                                            1L, 100.0, 100.0, 1.5, 100.0, 2),
                                    new CoordinateUpdateRequest.Payload(
                                            2L, 100.0, 100.0, 1.5, 100.0, 1)));

            // when
            coordinateService.updateCoordinate(1L, request);

            // then
            Coordinate coordinate =
                    transactionUtil.getResult(
                            () -> {
                                Coordinate loadedCoordinate =
                                        coordinateRepository.findById(1L).get();
                                loadedCoordinate.getCoordinateClothes().get(0);
                                return loadedCoordinate;
                            });

            Assertions.assertAll(
                    () ->
                            assertThat(coordinate)
                                    .extracting("imageUrl", "name", "memo")
                                    .containsExactly(
                                            "updatedTestUrl", "updatedTestName", "updatedTestMemo"),
                    () ->
                            assertThat(coordinate.getCoordinateClothes())
                                    .extracting(
                                            cc -> cc.getCloth().getId(),
                                            cc -> cc.getLocation().getLocationX(),
                                            cc -> cc.getLocation().getLocationY(),
                                            CoordinateCloth::getRatio,
                                            CoordinateCloth::getDegree,
                                            CoordinateCloth::getOrder)
                                    .containsExactlyInAnyOrder(
                                            tuple(1L, 100.0, 100.0, 1.5, 100.0, 2),
                                            tuple(2L, 100.0, 100.0, 1.5, 100.0, 1)));
        }

        @Test
        void 기존의_등록된_옷을_제외하고_요청하면_CoordinateCloth를_삭제한다() {
            // given
            CoordinateUpdateRequest request =
                    new CoordinateUpdateRequest(
                            "updatedTestUrl",
                            "updatedTestName",
                            "updatedTestMemo",
                            List.of(
                                    new CoordinateUpdateRequest.Payload(
                                            2L, 100.0, 100.0, 1.5, 100.0, 1)));

            // when
            coordinateService.updateCoordinate(1L, request);

            // then

            assertThat(coordinateClothRepository.findById(1L).isPresent()).isFalse();
        }

        @Test
        void 새로운_옷을_요청하면_CoordinateCloth를_생성한다() {
            // given
            CoordinateUpdateRequest request =
                    new CoordinateUpdateRequest(
                            "updatedTestUrl",
                            "updatedTestName",
                            "updatedTestMemo",
                            List.of(
                                    new CoordinateUpdateRequest.Payload(
                                            2L, 100.0, 100.0, 1.5, 100.0, 1),
                                    new CoordinateUpdateRequest.Payload(
                                            3L, 100.0, 100.0, 1.5, 100.0, 2)));

            // when
            coordinateService.updateCoordinate(1L, request);

            // then
            assertThat(coordinateClothRepository.findById(3L).orElseThrow())
                    .extracting(
                            "cloth.id",
                            "location.locationX",
                            "location.locationY",
                            "ratio",
                            "degree",
                            "order")
                    .containsExactly(3L, 100.0, 100.0, 1.5, 100.0, 2);
        }

        @Test
        void 코디_사진을_수정하면_기존의_사진을_S3에서_삭제하는_이벤트를_발행한다() {
            // given
            CoordinateUpdateRequest request =
                    new CoordinateUpdateRequest(
                            "updatedTestUrl",
                            "updatedTestName",
                            "updatedTestMemo",
                            List.of(
                                    new CoordinateUpdateRequest.Payload(
                                            2L, 100.0, 100.0, 1.5, 100.0, 1)));

            // when
            coordinateService.updateCoordinate(1L, request);

            // then
            var events = applicationEvents.stream(ImageDeleteEvent.class).toList();
            assertThat(events).hasSize(1);
            assertThat(events.getFirst().imageUrl()).isEqualTo("testUrl");
        }

        @Test
        void 코디_사진을_수정하지_않으면_기존의_사진을_S3에서_삭제하는_이벤트를_발행하지_않는다() {
            // given
            CoordinateUpdateRequest request =
                    new CoordinateUpdateRequest(
                            "testUrl",
                            "updatedTestName",
                            "updatedTestMemo",
                            List.of(
                                    new CoordinateUpdateRequest.Payload(
                                            2L, 100.0, 100.0, 1.5, 100.0, 1)));

            // when
            coordinateService.updateCoordinate(1L, request);

            // then
            var events = applicationEvents.stream(ImageDeleteEvent.class).toList();
            assertThat(events).isEmpty();
        }

        @Test
        void 옷의_ORDER가_유효하지_않은_경우_에외가_발생한다() {
            // given
            CoordinateUpdateRequest request =
                    new CoordinateUpdateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            List.of(
                                    new CoordinateUpdateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 1),
                                    new CoordinateUpdateRequest.Payload(
                                            2L, 100.5, 200.25, 1.0, 50.0, 3)));
            // when & then
            assertThatThrownBy(() -> coordinateService.updateCoordinate(1L, request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CoordinateErrorCode.INVALID_ORDER.getMessage());
        }

        @Test
        void 옷을_10개_초과로_등록하는_경우_예외가_발생한다() {
            // given
            CoordinateUpdateRequest request =
                    new CoordinateUpdateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            List.of(
                                    new CoordinateUpdateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 1),
                                    new CoordinateUpdateRequest.Payload(
                                            2L, 100.5, 200.25, 1.0, 50.0, 2),
                                    new CoordinateUpdateRequest.Payload(
                                            3L, 100.5, 200.25, 1.0, 50.0, 3),
                                    new CoordinateUpdateRequest.Payload(
                                            5L, 100.5, 200.25, 1.0, 50.0, 4),
                                    new CoordinateUpdateRequest.Payload(
                                            6L, 100.5, 200.25, 1.0, 50.0, 5),
                                    new CoordinateUpdateRequest.Payload(
                                            7L, 100.5, 200.25, 1.0, 50.0, 6),
                                    new CoordinateUpdateRequest.Payload(
                                            8L, 100.5, 200.25, 1.0, 50.0, 7),
                                    new CoordinateUpdateRequest.Payload(
                                            9L, 100.5, 200.25, 1.0, 50.0, 8),
                                    new CoordinateUpdateRequest.Payload(
                                            10L, 100.5, 200.25, 1.0, 50.0, 9),
                                    new CoordinateUpdateRequest.Payload(
                                            11L, 100.5, 200.25, 1.0, 50.0, 10),
                                    new CoordinateUpdateRequest.Payload(
                                            12L, 100.5, 200.25, 1.0, 50.0, 11)));

            // when & then
            assertThatThrownBy(() -> coordinateService.updateCoordinate(1L, request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CoordinateErrorCode.CLOTHES_OVER_COORDINATION_LIMIT.getMessage());
        }

        @Test
        void 중복된_옷을_입력하면_예외가_발생한다() {
            // given
            CoordinateUpdateRequest request =
                    new CoordinateUpdateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            List.of(
                                    new CoordinateUpdateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 1),
                                    new CoordinateUpdateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 2)));

            // when & then
            assertThatThrownBy(() -> coordinateService.updateCoordinate(1L, request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(ClothErrorCode.DUPLICATED_CLOTH.getMessage());
        }

        @Test
        void 존재하지_않는_옷을_포함하는_경우_예외가_발생한다() {
            // given
            CoordinateUpdateRequest request =
                    new CoordinateUpdateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            List.of(
                                    new CoordinateUpdateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 1),
                                    new CoordinateUpdateRequest.Payload(
                                            999L, 100.5, 200.25, 1.0, 50.0, 3)));

            // when & then
            assertThatThrownBy(() -> coordinateService.updateCoordinate(1L, request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(ClothErrorCode.ClOTH_NOT_FOUND.getMessage());
        }

        @Test
        void 나의_옷이_아닌_옷이_포함된_경우_예외가_발생한다() {
            // given
            CoordinateUpdateRequest request =
                    new CoordinateUpdateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            List.of(
                                    new CoordinateUpdateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 1),
                                    new CoordinateUpdateRequest.Payload(
                                            4L, 100.5, 200.25, 1.0, 50.0, 2)));
            // when & then
            assertThatThrownBy(() -> coordinateService.updateCoordinate(1L, request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(ClothErrorCode.NOT_CLOTH_OWNER.getMessage());
        }

        @Test
        void 존재하지_않는_코디를_입력하면_예외가_발생한다() {
            // given
            CoordinateUpdateRequest request =
                    new CoordinateUpdateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            List.of(
                                    new CoordinateUpdateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 1),
                                    new CoordinateUpdateRequest.Payload(
                                            2L, 100.5, 200.25, 1.0, 50.0, 2)));

            // when & then
            assertThatThrownBy(() -> coordinateService.updateCoordinate(999L, request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CoordinateErrorCode.COORDINATE_NOT_FOUND.getMessage());
        }

        @Test
        void 나의_코디가_아닌_경우_예외가_발생한다() {
            // given
            CoordinateUpdateRequest request =
                    new CoordinateUpdateRequest(
                            "testUrl",
                            "testName",
                            "testMemo",
                            List.of(
                                    new CoordinateUpdateRequest.Payload(
                                            1L, 100.5, 200.25, 1.0, 50.0, 1),
                                    new CoordinateUpdateRequest.Payload(
                                            2L, 100.5, 200.25, 1.0, 50.0, 2)));

            // when & then
            assertThatThrownBy(() -> coordinateService.updateCoordinate(2L, request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CoordinateErrorCode.NOT_COORDINATE_OWNER.getMessage());
        }
    }

    @Nested
    class 코디를_삭제할_때 {

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

            LookBook lookBook1 = LookBook.createLookBook("testName1", member1);
            LookBook lookBook2 = LookBook.createLookBook("testName2", member2);
            lookBookRepository.saveAll(List.of(lookBook1, lookBook2));

            Category category = Category.createCategory("testCategory", null);
            categoryRepository.save(category);

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
                            member1);
            Cloth cloth3 =
                    Cloth.createCloth(
                            "testImageUrl3",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member1);
            Cloth cloth4 =
                    Cloth.createCloth(
                            "testImageUrl4",
                            null,
                            null,
                            null,
                            List.of(Season.SPRING),
                            category,
                            member2);

            clothRepository.saveAll(List.of(cloth1, cloth2, cloth3, cloth4));

            Coordinate coordinate1 =
                    Coordinate.createCoordinateManual(
                            "testName", "testMemo", "testUrl", member1, lookBook1);
            Coordinate coordinate2 =
                    Coordinate.createCoordinateManual(
                            "testName", "testMemo", "testUrl", member2, lookBook2);
            Coordinate coordinate3 = Coordinate.createDailyCoordinate("testImageUrl", member1);
            Coordinate coordinate4 = Coordinate.createDailyCoordinate("testImageUrl", member1);
            coordinate4.addToDailyCoordinateToLookBook("testName", "testMemo", lookBook1);
            coordinateRepository.saveAll(
                    List.of(coordinate1, coordinate2, coordinate3, coordinate4));

            CoordinateCloth coordinateCloth1 =
                    CoordinateCloth.createCoordinateCloth(
                            1.0, 1.0, 1.0, 50.0, 1, coordinate1, cloth1);
            CoordinateCloth coordinateCloth2 =
                    CoordinateCloth.createCoordinateCloth(
                            2.0, 2.0, 2.0, 60.0, 2, coordinate1, cloth2);
            CoordinateCloth coordinateCloth3 =
                    CoordinateCloth.createCoordinateCloth(
                            2.0, 2.0, 2.0, 60.0, 2, coordinate4, cloth1);
            coordinateClothRepository.saveAll(
                    List.of(coordinateCloth1, coordinateCloth2, coordinateCloth3));
        }

        @Test
        void 수동으로_만들어진_코디는_유관_정보를_모두_삭제한다() {
            // when
            coordinateService.deleteCoordinate(1L);

            // then

            var events = applicationEvents.stream(ImageDeleteEvent.class).toList();

            Assertions.assertAll(
                    () -> assertThat(coordinateRepository.findById(1L).isPresent()).isFalse(),
                    () -> assertThat(events).hasSize(1),
                    () -> assertThat(events.getFirst().imageUrl()).isEqualTo("testUrl"),
                    () -> assertThat(coordinateClothRepository.findById(1L).isPresent()).isFalse(),
                    () -> assertThat(coordinateClothRepository.findById(2L).isPresent()).isFalse());
        }

        @Test
        void 오늘의_코디에서_추가된_코디는_룩북에서만_제거한다() {
            // when
            coordinateService.deleteCoordinate(4L);

            // then
            Assertions.assertAll(
                    () ->
                            assertThat(coordinateRepository.findById(3L).orElseThrow())
                                    .extracting("name", "memo", "liked", "lookBook")
                                    .containsExactly(null, null, false, null),
                    () -> assertThat(coordinateClothRepository.findById(3L)).isPresent());
        }

        @Test
        void 존재하지_않는_코디를_입력하면_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> coordinateService.deleteCoordinate(999L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CoordinateErrorCode.COORDINATE_NOT_FOUND.getMessage());
        }

        @Test
        void 나의_코디가_아닌_경우_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> coordinateService.deleteCoordinate(2L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CoordinateErrorCode.NOT_COORDINATE_OWNER.getMessage());
        }

        @Test
        void 룩북에_속하지_않는_오늘의_코디를_삭제하면_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> coordinateService.deleteCoordinate(3L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CoordinateErrorCode.COORDINATE_NOT_IN_LOOK_BOOK.getMessage());
        }
    }

    @Nested
    class 오늘의_코디_목록을_조회할_때 {

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

            LookBook lookBook = LookBook.createLookBook("testName", member1);
            lookBookRepository.save(lookBook);

            Coordinate coordinate1 = Coordinate.createDailyCoordinate("testImageUrl1", member1);
            Coordinate coordinate2 = Coordinate.createDailyCoordinate("testImageUrl2", member1);
            Coordinate coordinate3 = Coordinate.createDailyCoordinate("testImageUrl3", member1);

            /** 이와 같은 조건이 보이지 않습니다. */
            Coordinate customCoordinate =
                    Coordinate.createCoordinateManual(
                            "testName", "testMemo", "testImageUrl4", member1, lookBook);
            Coordinate dailyCoordinateAlreadyAdded =
                    Coordinate.createDailyCoordinate("testImageUrl5", member1);
            dailyCoordinateAlreadyAdded.addToDailyCoordinateToLookBook(
                    "testName", "testMemo", lookBook);
            coordinateRepository.saveAll(
                    List.of(
                            coordinate1,
                            coordinate2,
                            coordinate3,
                            customCoordinate,
                            dailyCoordinateAlreadyAdded));
        }

        @Test
        void 정렬_조건이_ASC이면_coordinateId를_오름차순으로_조회한다() {
            // when
            SliceResponse<DailyCoordinateListResponse> response =
                    coordinateService.getDailyCoordinates(null, 3, SortDirection.ASC);

            // then
            assertThat(response.content()).extracting("coordinateId").containsExactly(1L, 2L, 3L);
        }

        @Test
        void 정렬_조건이_DESC면_coordinateId를_내림차순으로_조회한다() {
            // when
            SliceResponse<DailyCoordinateListResponse> response =
                    coordinateService.getDailyCoordinates(null, 3, SortDirection.DESC);

            // then
            assertThat(response.content()).extracting("coordinateId").containsExactly(3L, 2L, 1L);
        }

        @Test
        void lastCoordinateId를_입력하면_다음_coordinate_부터_조회한다() {
            // when
            SliceResponse<DailyCoordinateListResponse> response =
                    coordinateService.getDailyCoordinates(1L, 2, SortDirection.ASC);

            // then
            assertThat(response.content()).extracting("coordinateId").containsExactly(2L, 3L);
        }

        @Test
        void 조건에_맞는_오늘의_코디가_없는_경우_빈_리스트를_조회한다() {
            // given
            Member member = memberRepository.findById(2L).orElseThrow();
            given(memberUtil.getCurrentMember()).willReturn(member);

            // when
            SliceResponse<DailyCoordinateListResponse> response =
                    coordinateService.getDailyCoordinates(null, 3, SortDirection.ASC);

            // when & then
            Assertions.assertAll(
                    () -> assertThat(response.content().size()).isZero(),
                    () -> assertThat(response.isLast()).isTrue());
        }

        @Test
        void 마지막_페이지인_경우_isLast를_true로_반환한다() {
            // when
            SliceResponse<DailyCoordinateListResponse> response =
                    coordinateService.getDailyCoordinates(null, 3, SortDirection.ASC);

            // then
            Assertions.assertAll(
                    () -> assertThat(response.content().size()).isEqualTo(3),
                    () -> assertThat(response.isLast()).isTrue());
        }

        @Test
        void 마지막_페이지가_아닌_경우_isLast를_false로_반환한다() {
            // when
            SliceResponse<DailyCoordinateListResponse> response =
                    coordinateService.getDailyCoordinates(null, 2, SortDirection.ASC);

            // then
            Assertions.assertAll(
                    () -> assertThat(response.content().size()).isEqualTo(2),
                    () -> assertThat(response.isLast()).isFalse());
        }

        @Test
        void 오늘의_코디가_아니거나_이미_룩북에_추가된_오늘의_코디는_조회되지_않는다() {
            // when
            SliceResponse<DailyCoordinateListResponse> response =
                    coordinateService.getDailyCoordinates(3L, 2, SortDirection.ASC);

            // then
            Assertions.assertAll(
                    () -> assertThat(response.content().size()).isZero(),
                    () -> assertThat(response.isLast()).isTrue());
        }
    }

    @Nested
    class 코디_Preview를_조회할_때 {

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

            LookBook lookBook1 = LookBook.createLookBook("testName1", member1);
            LookBook lookBook2 = LookBook.createLookBook("testName2", member2);
            lookBookRepository.saveAll(List.of(lookBook1, lookBook2));

            Coordinate coordinate1 =
                    Coordinate.createCoordinateManual(
                            "testName1", "testMemo1", "testImageUrl1", member1, lookBook1);
            Coordinate coordinate2 =
                    Coordinate.createCoordinateManual(
                            "testName2", "testMemo2", "testImageUrl2", member2, lookBook2);
            Coordinate coordinate3 =
                    Coordinate.createCoordinateManual(
                            "testName3", "testMemo3", "testImageUrl3", member1, null);

            coordinateRepository.saveAll(List.of(coordinate1, coordinate2, coordinate3));
        }

        @Test
        void 유효한_요청이면_Preview를_반환한다() {
            // when
            CoordinatePreviewResponse response = coordinateService.getCoordinatePreview(1L);

            // then
            assertThat(response)
                    .extracting("coordinateId", "imageUrl", "coordinateName", "coordinateMemo")
                    .containsExactly(1L, "testImageUrl1", "testName1", "testMemo1");
        }

        @Test
        void 코디가_존재하지_않으면_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> coordinateService.getCoordinatePreview(999L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CoordinateErrorCode.COORDINATE_NOT_FOUND.getMessage());
        }

        @Test
        void 나의_코디가_아닌_경우_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> coordinateService.getCoordinatePreview(2L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CoordinateErrorCode.NOT_COORDINATE_OWNER.getMessage());
        }

        @Test
        void 룩북에_속한_코디가_아닌_경우_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> coordinateService.getCoordinatePreview(3L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CoordinateErrorCode.COORDINATE_NOT_IN_LOOK_BOOK.getMessage());
        }
    }

    @Nested
    class 코디_Details를_조회할_때 {

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

            LookBook lookBook1 = LookBook.createLookBook("testName1", member1);
            LookBook lookBook2 = LookBook.createLookBook("testName2", member2);
            lookBookRepository.saveAll(List.of(lookBook1, lookBook2));

            Coordinate coordinate1 =
                    Coordinate.createCoordinateManual(
                            "testName1", "testMemo1", "testImageUrl1", member1, lookBook1);
            Coordinate coordinate2 =
                    Coordinate.createCoordinateManual(
                            "testName2", "testMemo2", "testImageUrl2", member2, lookBook2);
            Coordinate coordinate3 =
                    Coordinate.createCoordinateManual(
                            "testName3", "testMemo3", "testImageUrl3", member1, null);
            coordinateRepository.saveAll(List.of(coordinate1, coordinate2, coordinate3));

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
                            member1);
            clothRepository.saveAll(List.of(cloth1, cloth2));

            CoordinateCloth coordinateCloth1 =
                    CoordinateCloth.createCoordinateCloth(
                            50.1, 120.1, 1.5, 240.1, 1, coordinate1, cloth1);

            CoordinateCloth coordinateCloth2 =
                    CoordinateCloth.createCoordinateCloth(
                            50.1, 120.1, 1.5, 240.1, 2, coordinate1, cloth2);

            coordinateClothRepository.saveAll(List.of(coordinateCloth1, coordinateCloth2));
        }

        @Test
        void 유효한_요청이면_코디_Details를_반환한다() {
            // when
            List<CoordinateDetailsListResponse> response =
                    coordinateService.getCoordinateDetails(1L);

            // then
            assertThat(response)
                    .extracting(
                            CoordinateDetailsListResponse::coordinateClothId,
                            CoordinateDetailsListResponse::locationX,
                            CoordinateDetailsListResponse::locationY,
                            CoordinateDetailsListResponse::ratio,
                            CoordinateDetailsListResponse::degree,
                            CoordinateDetailsListResponse::order,
                            CoordinateDetailsListResponse::clothId,
                            CoordinateDetailsListResponse::imageUrl,
                            CoordinateDetailsListResponse::brand,
                            CoordinateDetailsListResponse::name,
                            CoordinateDetailsListResponse::category,
                            CoordinateDetailsListResponse::parentCategory)
                    .containsExactly(
                            tuple(
                                    1L,
                                    50.1,
                                    120.1,
                                    1.5,
                                    240.1,
                                    1,
                                    1L,
                                    "testImageUrl1",
                                    null,
                                    null,
                                    "testCategory",
                                    "testParentCategory"),
                            tuple(
                                    2L,
                                    50.1,
                                    120.1,
                                    1.5,
                                    240.1,
                                    2,
                                    2L,
                                    "testImageUrl2",
                                    null,
                                    null,
                                    "testCategory",
                                    "testParentCategory"));
        }

        @Test
        void 코디가_존재하지_않으면_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> coordinateService.getCoordinateDetails(999L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CoordinateErrorCode.COORDINATE_NOT_FOUND.getMessage());
        }

        @Test
        void 나의_코디가_아닌_경우_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> coordinateService.getCoordinateDetails(2L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CoordinateErrorCode.NOT_COORDINATE_OWNER.getMessage());
        }

        @Test
        void 룩북에_속한_코디가_아닌_경우_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> coordinateService.getCoordinateDetails(3L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CoordinateErrorCode.COORDINATE_NOT_IN_LOOK_BOOK.getMessage());
        }
    }

    @Nested
    class 코디_좋아요를_토글할_때 {

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

            LookBook lookBook = LookBook.createLookBook("testName1", member1);
            lookBookRepository.save(lookBook);

            Category category = Category.createCategory("testCategory", null);
            categoryRepository.save(category);

            Coordinate coordinate1 =
                    Coordinate.createCoordinateManual(
                            "testName", "testMemo", "testUrl", member1, lookBook);
            Coordinate coordinate2 =
                    Coordinate.createCoordinateManual(
                            "testName", "testMemo", "testUrl", member1, lookBook);
            Coordinate coordinate3 =
                    Coordinate.createCoordinateManual(
                            "testName", "testMemo", "testUrl", member1, lookBook);
            Coordinate coordinate4 =
                    Coordinate.createCoordinateManual(
                            "testName", "testMemo", "testUrl", member1, lookBook);
            Coordinate coordinate5 =
                    Coordinate.createCoordinateManual(
                            "testName", "testMemo", "testUrl", member1, lookBook);
            Coordinate coordinate6 =
                    Coordinate.createCoordinateManual(
                            "testName", "testMemo", "testUrl", member1, lookBook);
            coordinate1.toggleLike();
            coordinate2.toggleLike();
            coordinate3.toggleLike();
            coordinate4.toggleLike();

            Coordinate coordinate7 = Coordinate.createDailyCoordinate("testImageUrl", member1);
            Coordinate coordinate8 = Coordinate.createDailyCoordinate("testImageUrl", member2);

            coordinateRepository.saveAll(
                    List.of(
                            coordinate1,
                            coordinate2,
                            coordinate3,
                            coordinate4,
                            coordinate5,
                            coordinate6,
                            coordinate7,
                            coordinate8));
        }

        @Test
        void 이미_좋아요_상태면_좋아요를_취소한다() {
            // when
            coordinateService.toggleCoordinateLike(1L);

            // then
            assertThat(coordinateRepository.findById(1L).get().getLiked()).isFalse();
        }

        @Test
        void 좋아요_상태가_아니면_좋아요로_전환한다() {
            // when
            coordinateService.toggleCoordinateLike(5L);

            // then
            assertThat(coordinateRepository.findById(5L).get().getLiked()).isTrue();
        }

        @Test
        void 나의_코디가_아니면_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> coordinateService.toggleCoordinateLike(8L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CoordinateErrorCode.NOT_COORDINATE_OWNER.getMessage());
        }

        @Test
        void 룩북에_속하지_않는_코디에_대해서_요청하면_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> coordinateService.toggleCoordinateLike(7L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CoordinateErrorCode.COORDINATE_NOT_IN_LOOK_BOOK.getMessage());
        }

        @Test
        void 다섯개_이상의_코디에_좋아요를_누를_경우_예외가_발생한다() {
            // given
            coordinateService.toggleCoordinateLike(5L);

            // when & then
            assertThatThrownBy(() -> coordinateService.toggleCoordinateLike(6L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CoordinateErrorCode.COORDINATE_LIKE_LIMIT.getMessage());
        }
    }

    @Nested
    class 최애_코디를_조회할_때 {

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

            LookBook lookBook1 = LookBook.createLookBook("testName1", member1);
            LookBook lookBook2 = LookBook.createLookBook("testName2", member2);
            lookBookRepository.saveAll(List.of(lookBook1, lookBook2));

            Category category = Category.createCategory("testCategory", null);
            categoryRepository.save(category);

            Coordinate coordinate1 =
                    Coordinate.createCoordinateManual(
                            "testName1", "testMemo1", "testUrl1", member1, lookBook1);
            Coordinate coordinate2 =
                    Coordinate.createCoordinateManual(
                            "testName2", "testMemo2", "testUrl2", member1, lookBook1);
            Coordinate coordinate3 =
                    Coordinate.createCoordinateManual(
                            "testName3", "testMemo3", "testUrl3", member1, lookBook1);
            Coordinate coordinate4 =
                    Coordinate.createCoordinateManual(
                            "testName4", "testMemo4", "testUrl4", member1, lookBook1);
            coordinate1.toggleLike();
            coordinate2.toggleLike();

            Coordinate coordinate5 =
                    Coordinate.createCoordinateManual(
                            "testName4", "testMemo4", "testUrl4", member1, lookBook1);
            Coordinate coordinate6 =
                    Coordinate.createCoordinateManual(
                            "testName4", "testMemo4", "testUrl4", member1, lookBook1);
            coordinateRepository.saveAll(
                    List.of(
                            coordinate1,
                            coordinate2,
                            coordinate3,
                            coordinate4,
                            coordinate5,
                            coordinate6));
        }

        @Test
        void 유효한_요청이면_좋아요한_코디를_반환한다() {
            // when
            List<FavoriteCoordinateResponse> responses =
                    coordinateService.getFavoriteCoordinates(null);

            // then
            assertThat(responses)
                    .extracting("coordinateId", "imageUrl", "coordinateName")
                    .containsExactly(
                            tuple(1L, "testUrl1", "testName1"), tuple(2L, "testUrl2", "testName2"));
        }

        @Test
        void 좋아요한_코디가_없는_경우_빈_리스트를_반환한다() {
            // given
            Member member = memberRepository.findById(2L).orElseThrow();
            given(memberUtil.getCurrentMember()).willReturn(member);

            // when
            List<FavoriteCoordinateResponse> responses =
                    coordinateService.getFavoriteCoordinates(null);

            // then
            assertThat(responses).isEmpty();
        }

        @Test
        void memberId를_전달하면_해당_회원의_좋아요한_코디를_반환한다() {
            // when
            List<FavoriteCoordinateResponse> responses =
                    coordinateService.getFavoriteCoordinates(1L);

            // then
            assertThat(responses)
                    .extracting("coordinateId", "imageUrl", "coordinateName")
                    .containsExactly(
                            tuple(1L, "testUrl1", "testName1"), tuple(2L, "testUrl2", "testName2"));
        }

        @Test
        void 비공개_계정의_memberId로_조회하면_예외가_발생한다() {
            // given
            Member member = memberRepository.findById(2L).orElseThrow();
            member.changeVisibility();
            memberRepository.saveAndFlush(member);

            // when & then
            assertThatThrownBy(() -> coordinateService.getFavoriteCoordinates(2L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(MemberErrorCode.PRIVATE_MEMBER_ACCESS_DENIED.getMessage());
        }

        @Test
        void 차단_관계의_memberId로_조회하면_예외가_발생한다() {
            // given
            Member currentMember = memberRepository.findById(1L).orElseThrow();
            Member targetMember = memberRepository.findById(2L).orElseThrow();
            blockRepository.save(Block.createBlock(currentMember, targetMember));

            // when & then
            assertThatThrownBy(() -> coordinateService.getFavoriteCoordinates(2L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(MemberErrorCode.BLOCKED_MEMBER_ACCESS_DENIED.getMessage());
        }
    }

    @Nested
    class 오늘의_코디_Preview를_조회할_때 {

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

            Coordinate coordinate1 = Coordinate.createDailyCoordinate("testImageUrl1", member1);
            coordinateRepository.save(coordinate1);
        }

        @Test
        void 유효한_요청이면_Preview를_반환한다() {
            // when
            DailyCoordinatePreviewResponse response = coordinateService.getTodayCoordinatePreview();

            // then
            assertThat(response)
                    .extracting("coordinateId", "imageUrl", "date")
                    .containsExactly(1L, "testImageUrl1", LocalDate.now());
        }

        @Test
        void 오늘의_코디가_존재하지_않으면_예외가_발생한다() {
            // given
            Member member = memberRepository.findById(2L).orElseThrow();
            given(memberUtil.getCurrentMember()).willReturn(member);

            // when & then
            assertThatThrownBy(() -> coordinateService.getTodayCoordinatePreview())
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CoordinateErrorCode.DAILY_COORDINATE_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 오늘의_코디_Details를_조회할_때 {

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

            Coordinate coordinate1 = Coordinate.createDailyCoordinate("testImageUrl1", member1);
            coordinateRepository.save(coordinate1);

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
                            member1);
            clothRepository.saveAll(List.of(cloth1, cloth2));

            CoordinateCloth coordinateCloth1 =
                    CoordinateCloth.createCoordinateCloth(
                            50.1, 120.1, 1.5, 240.1, 1, coordinate1, cloth1);

            CoordinateCloth coordinateCloth2 =
                    CoordinateCloth.createCoordinateCloth(
                            50.1, 120.1, 1.5, 240.1, 2, coordinate1, cloth2);

            coordinateClothRepository.saveAll(List.of(coordinateCloth1, coordinateCloth2));
        }

        @Test
        void 유효한_요청이면_코디_Details를_반환한다() {
            // when
            List<CoordinateDetailsListResponse> response =
                    coordinateService.getTodayCoordinateDetails();

            // then
            assertThat(response)
                    .extracting(
                            CoordinateDetailsListResponse::coordinateClothId,
                            CoordinateDetailsListResponse::locationX,
                            CoordinateDetailsListResponse::locationY,
                            CoordinateDetailsListResponse::ratio,
                            CoordinateDetailsListResponse::degree,
                            CoordinateDetailsListResponse::order,
                            CoordinateDetailsListResponse::clothId,
                            CoordinateDetailsListResponse::imageUrl,
                            CoordinateDetailsListResponse::brand,
                            CoordinateDetailsListResponse::name,
                            CoordinateDetailsListResponse::category,
                            CoordinateDetailsListResponse::parentCategory)
                    .containsExactly(
                            tuple(
                                    1L,
                                    50.1,
                                    120.1,
                                    1.5,
                                    240.1,
                                    1,
                                    1L,
                                    "testImageUrl1",
                                    null,
                                    null,
                                    "testCategory",
                                    "testParentCategory"),
                            tuple(
                                    2L,
                                    50.1,
                                    120.1,
                                    1.5,
                                    240.1,
                                    2,
                                    2L,
                                    "testImageUrl2",
                                    null,
                                    null,
                                    "testCategory",
                                    "testParentCategory"));
        }

        @Test
        void 오늘의_코디가_존재하지_않으면_예외가_발생한다() {
            // given
            Member member = memberRepository.findById(2L).orElseThrow();
            given(memberUtil.getCurrentMember()).willReturn(member);

            // when & then
            assertThatThrownBy(() -> coordinateService.getTodayCoordinateDetails())
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CoordinateErrorCode.DAILY_COORDINATE_NOT_FOUND.getMessage());
        }
    }
}
