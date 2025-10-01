package org.clokey.domain.coordinate.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.clokey.cloth.entity.Cloth;
import org.clokey.coordinate.entity.Coordinate;
import org.clokey.coordinate.entity.CoordinateCloth;
import org.clokey.domain.cloth.exception.ClothErrorCode;
import org.clokey.domain.cloth.repository.ClothRepository;
import org.clokey.domain.coordinate.dto.request.DailyCoordinateCreateRequest;
import org.clokey.domain.coordinate.dto.response.DailyCoordinateCreateResponse;
import org.clokey.domain.coordinate.exception.CoordinateErrorCode;
import org.clokey.domain.coordinate.repository.CoordinateClothRepository;
import org.clokey.domain.coordinate.repository.CoordinateRepository;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.util.MemberUtil;
import org.clokey.member.entity.Member;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoordinateServiceImpl implements CoordinateService {

    private final MemberUtil memberUtil;

    private final CoordinateRepository coordinateRepository;
    private final ClothRepository clothRepository;
    private final CoordinateClothRepository coordinateClothRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    @Transactional
    public DailyCoordinateCreateResponse createDailyCoordinate(
            DailyCoordinateCreateRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        final Map<Long, Cloth> clothMap =
                clothRepository
                        .findAllById(
                                request.payloads().stream()
                                        .map(DailyCoordinateCreateRequest.Payload::clothId)
                                        .toList())
                        .stream()
                        .collect(Collectors.toMap(Cloth::getId, Function.identity()));

        validateAllClothesExist(request, clothMap);

        final List<Cloth> clothes =
                request.payloads().stream()
                        .map(payload -> clothMap.get(payload.clothId()))
                        .toList();

        validateSequentialOrders(request);
        validateExceedingCoordinationClothesLimit(request);
        validateDuplicatedClothes(clothes);
        validateAllClothesOwnership(currentMember, clothes);
        validateDailyCoordinateExist(currentMember.getId(), LocalDate.now());

        Coordinate coordinate =
                Coordinate.createDailyCoordinate(request.coordinateImageUrl(), currentMember);
        coordinateRepository.save(coordinate);
        saveDailyCoordinateToRedis(currentMember.getId(), coordinate.getId());

        List<CoordinateCloth> coordinateClothes =
                request.payloads().stream()
                        .map(
                                payload ->
                                        CoordinateCloth.createCoordinateCloth(
                                                payload.locationX(),
                                                payload.locationY(),
                                                payload.ratio(),
                                                payload.degree(),
                                                payload.order(),
                                                coordinate,
                                                clothMap.get(payload.clothId())))
                        .toList();

        coordinateClothRepository.saveAll(coordinateClothes);

        return DailyCoordinateCreateResponse.from(coordinate);
    }

    private void validateAllClothesExist(
            DailyCoordinateCreateRequest request, Map<Long, Cloth> clothMap) {
        boolean hasMissing =
                request.payloads().stream()
                        .map(DailyCoordinateCreateRequest.Payload::clothId)
                        .anyMatch(clothId -> !clothMap.containsKey(clothId));

        if (hasMissing) {
            throw new BaseCustomException(ClothErrorCode.ClOTH_NOT_FOUND);
        }
    }

    private void validateAllClothesOwnership(Member member, List<Cloth> clothes) {
        boolean containsClothesNotMine =
                clothes.stream()
                        .anyMatch(cloth -> !cloth.getMember().getId().equals(member.getId()));

        if (containsClothesNotMine) {
            throw new BaseCustomException(ClothErrorCode.NOT_CLOTH_OWNER);
        }
    }

    private void validateDailyCoordinateExist(Long memberId, LocalDate date) {
        if (hasDailyCoordinate(memberId, date)) {
            throw new BaseCustomException(CoordinateErrorCode.DAILY_COORDINATE_ALREADY_EXISTS);
        }
    }

    private void validateDuplicatedClothes(List<Cloth> clothes) {
        Set<Long> seen = new HashSet<>();
        clothes.stream()
                .map(Cloth::getId)
                .forEach(
                        id -> {
                            if (!seen.add(id)) {
                                throw new BaseCustomException(ClothErrorCode.DUPLICATED_CLOTH);
                            }
                        });
    }

    private void validateSequentialOrders(DailyCoordinateCreateRequest request) {
        List<Integer> orders =
                request.payloads().stream()
                        .map(DailyCoordinateCreateRequest.Payload::order)
                        .sorted()
                        .toList();

        boolean isSequential =
                IntStream.range(0, orders.size()).allMatch(i -> orders.get(i) == i + 1);

        if (!isSequential) {
            throw new BaseCustomException(CoordinateErrorCode.INVALID_ORDER);
        }
    }

    private void validateExceedingCoordinationClothesLimit(DailyCoordinateCreateRequest request) {
        if (request.payloads().size() > 10) {
            throw new BaseCustomException(CoordinateErrorCode.CLOTHES_OVER_COORDINATION_LIMIT);
        }
    }

    private Long getDailyCoordinateId(Long memberId, LocalDate date) {
        String key = String.format("dailyCoordinate:%d:%s", memberId, date);
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.valueOf(value) : null;
    }

    private boolean hasDailyCoordinate(Long memberId, LocalDate date) {
        String key = String.format("dailyCoordinate:%d:%s", memberId, date);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    private void saveDailyCoordinateToRedis(Long memberId, Long coordinateId) {
        String key = String.format("dailyCoordinate:%d:%s", memberId, LocalDate.now());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = LocalDate.now().atTime(LocalTime.MAX);
        Duration ttl = Duration.between(now, midnight);

        redisTemplate.opsForValue().set(key, coordinateId.toString(), ttl);
    }
}
