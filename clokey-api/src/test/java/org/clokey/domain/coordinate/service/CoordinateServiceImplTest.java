package org.clokey.domain.coordinate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.mockito.BDDMockito.given;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.clokey.IntegrationTest;
import org.clokey.RedisCleaner;
import org.clokey.TransactionUtil;
import org.clokey.category.entity.Category;
import org.clokey.cloth.entity.Cloth;
import org.clokey.coordinate.entity.Coordinate;
import org.clokey.coordinate.entity.CoordinateCloth;
import org.clokey.domain.category.repository.CategoryRepository;
import org.clokey.domain.cloth.exception.ClothErrorCode;
import org.clokey.domain.cloth.repository.ClothRepository;
import org.clokey.domain.coordinate.dto.request.DailyCoordinateCreateRequest;
import org.clokey.domain.coordinate.exception.CoordinateErrorCode;
import org.clokey.domain.coordinate.repository.CoordinateClothRepository;
import org.clokey.domain.coordinate.repository.CoordinateRepository;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.util.MemberUtil;
import org.clokey.member.entity.Member;
import org.clokey.member.entity.OauthInfo;
import org.clokey.member.enums.OauthProvider;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class CoordinateServiceImplTest extends IntegrationTest {

    @Autowired private TransactionUtil transactionUtil;
    @Autowired private RedisCleaner redisCleaner;

    @Autowired private CoordinateRepository coordinateRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private CoordinateClothRepository coordinateClothRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ClothRepository clothRepository;
    @Autowired private CoordinateService coordinateService;
    @Autowired private StringRedisTemplate redisTemplate;

    @MockitoBean private MemberUtil memberUtil;

    @Nested
    class 오늘의_코디를_생성할_때 {

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

            Category category = Category.createCategory("testCategory", null);
            categoryRepository.save(category);

            Cloth cloth1 = Cloth.createCloth("testImageUrl1", category, member1);
            Cloth cloth2 = Cloth.createCloth("testImageUrl2", category, member1);
            Cloth cloth3 = Cloth.createCloth("testImageUrl3", category, member2);

            Cloth cloth4 = Cloth.createCloth("testImageUrl4", category, member1);
            Cloth cloth5 = Cloth.createCloth("testImageUrl5", category, member1);
            Cloth cloth6 = Cloth.createCloth("testImageUrl6", category, member1);
            Cloth cloth7 = Cloth.createCloth("testImageUrl7", category, member1);
            Cloth cloth8 = Cloth.createCloth("testImageUrl8", category, member1);
            Cloth cloth9 = Cloth.createCloth("testImageUrl9", category, member1);
            Cloth cloth10 = Cloth.createCloth("testImageUrl10", category, member1);
            Cloth cloth11 = Cloth.createCloth("testImageUrl11", category, member1);
            Cloth cloth12 = Cloth.createCloth("testImageUrl12", category, member1);
            clothRepository.saveAll(
                    List.of(
                            cloth1, cloth2, cloth3, cloth4, cloth5, cloth6, cloth7, cloth8, cloth9,
                            cloth10, cloth11, cloth12));
        }

        @AfterEach
        void cleanUp() {
            redisCleaner.flushAll();
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

            String key = String.format("dailyCoordinate:%d:%s", 1L, LocalDate.now());
            String savedValue = redisTemplate.opsForValue().get(key);

            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);

            // 오늘 자정까지 남은 예상 TTL (초 단위)
            long expectedTtl =
                    Duration.between(LocalDateTime.now(), LocalDate.now().atTime(LocalTime.MAX))
                            .getSeconds();

            Assertions.assertAll(
                    () ->
                            assertThat(coordinate)
                                    .extracting("imageUrl", "member.id")
                                    .containsExactly("testUrl", 1L),
                    () ->
                            assertThat(coordinate.getCoordinateClothes())
                                    .extracting(
                                            cc -> cc.getCloth().getId(),
                                            CoordinateCloth::getOrder,
                                            CoordinateCloth::getRatio)
                                    .containsExactlyInAnyOrder(
                                            tuple(1L, 1, 1.0), tuple(2L, 2, 1.0)),
                    () -> assertThat(savedValue).isEqualTo(coordinate.getId().toString()),
                    () -> assertThat(ttl).isCloseTo(expectedTtl, within(5L)) // 오차 범위 5초
                    );
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

            String key = String.format("dailyCoordinate:%d:%s", 1L, LocalDate.now());
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime midnight = LocalDate.now().atTime(LocalTime.MAX);
            Duration ttl = Duration.between(now, midnight);
            redisTemplate.opsForValue().set(key, "1", ttl);

            // when & then
            assertThatThrownBy(() -> coordinateService.createDailyCoordinate(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CoordinateErrorCode.DAILY_COORDINATE_ALREADY_EXISTS.getMessage());
        }
    }
}
