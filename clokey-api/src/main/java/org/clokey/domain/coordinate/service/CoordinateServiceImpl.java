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
import org.clokey.domain.coordinate.dto.request.CoordinateAutoCreateRequest;
import org.clokey.domain.coordinate.dto.request.CoordinateManualCreateRequest;
import org.clokey.domain.coordinate.dto.request.DailyCoordinateCreateRequest;
import org.clokey.domain.coordinate.dto.response.CoordinateCreateResponse;
import org.clokey.domain.coordinate.exception.CoordinateErrorCode;
import org.clokey.domain.coordinate.repository.CoordinateClothRepository;
import org.clokey.domain.coordinate.repository.CoordinateRepository;
import org.clokey.domain.lookbook.exception.LookBookErrorCode;
import org.clokey.domain.lookbook.repository.LookBookRepository;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.util.MemberUtil;
import org.clokey.lookbook.entity.LookBook;
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
    private final LookBookRepository lookBookRepository;
    private final CoordinateClothRepository coordinateClothRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    @Transactional
    public CoordinateCreateResponse createDailyCoordinate(DailyCoordinateCreateRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        final Map<Long, Cloth> clothMap =
                clothRepository
                        .findAllById(
                                request.payloads().stream()
                                        .map(DailyCoordinateCreateRequest.Payload::clothId)
                                        .toList())
                        .stream()
                        .collect(Collectors.toMap(Cloth::getId, Function.identity()));

        validateAllClothesExist(
                request.payloads().stream()
                        .map(DailyCoordinateCreateRequest.Payload::clothId)
                        .toList(),
                clothMap);

        final List<Cloth> clothes =
                request.payloads().stream()
                        .map(payload -> clothMap.get(payload.clothId()))
                        .toList();

        validateSequentialOrders(
                request.payloads().stream()
                        .map(DailyCoordinateCreateRequest.Payload::order)
                        .sorted()
                        .toList());
        validateExceedingCoordinationClothesLimit(request.payloads());
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

        return CoordinateCreateResponse.from(coordinate);
    }

    @Override
    @Transactional
    public CoordinateCreateResponse createCoordinateManual(CoordinateManualCreateRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        final Map<Long, Cloth> clothMap =
                clothRepository
                        .findAllById(
                                request.payloads().stream()
                                        .map(CoordinateManualCreateRequest.Payload::clothId)
                                        .toList())
                        .stream()
                        .collect(Collectors.toMap(Cloth::getId, Function.identity()));

        validateAllClothesExist(
                request.payloads().stream()
                        .map(CoordinateManualCreateRequest.Payload::clothId)
                        .toList(),
                clothMap);

        final List<Cloth> clothes =
                request.payloads().stream()
                        .map(payload -> clothMap.get(payload.clothId()))
                        .toList();

        validateSequentialOrders(
                request.payloads().stream()
                        .map(CoordinateManualCreateRequest.Payload::order)
                        .sorted()
                        .toList());
        validateExceedingCoordinationClothesLimit(request.payloads());
        validateDuplicatedClothes(clothes);
        validateAllClothesOwnership(currentMember, clothes);

        final LookBook lookBook = getLookBookById(request.lookBookId());

        validateLookBookOwner(lookBook, currentMember.getId());

        Coordinate coordinate =
                Coordinate.createCoordinateManual(
                        request.name(),
                        request.memo(),
                        request.coordinateImageUrl(),
                        currentMember,
                        lookBook);
        coordinateRepository.save(coordinate);

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

        return CoordinateCreateResponse.from(coordinate);
    }

    @Override
    @Transactional
    public CoordinateCreateResponse createCoordinateAuto(CoordinateAutoCreateRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        final Coordinate dailyCoordinate = getCoordinateById(request.dailyCoordinateId());
        final LookBook lookBook = getLookBookById(request.lookBookId());

        validateCoordinateOwner(dailyCoordinate, currentMember.getId());
        validateLookBookOwner(lookBook, currentMember.getId());
        validateDailyCoordinate(dailyCoordinate);

        dailyCoordinate.addToDailyCoordinateToLookBook(request.name(), request.memo(), lookBook);

        return CoordinateCreateResponse.from(dailyCoordinate);
    }

    private void validateAllClothesExist(List<Long> clothIds, Map<Long, Cloth> clothMap) {
        boolean hasMissing = clothIds.stream().anyMatch(clothId -> !clothMap.containsKey(clothId));

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

    private void validateSequentialOrders(List<Integer> orders) {
        boolean isSequential =
                IntStream.range(0, orders.size()).allMatch(i -> orders.get(i) == i + 1);

        if (!isSequential) {
            throw new BaseCustomException(CoordinateErrorCode.INVALID_ORDER);
        }
    }

    private <T> void validateExceedingCoordinationClothesLimit(List<T> payloads) {
        if (payloads.size() > 10) {
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

    private void validateLookBookOwner(LookBook lookBook, Long memberId) {
        if (!Objects.equals(lookBook.getMember().getId(), memberId)) {
            throw new BaseCustomException(LookBookErrorCode.NOT_LOOK_BOOK_OWNER);
        }
    }

    private void validateCoordinateOwner(Coordinate coordinate, Long memberId) {
        if (!Objects.equals(coordinate.getMember().getId(), memberId)) {
            throw new BaseCustomException(CoordinateErrorCode.NOT_COORDINATE_OWNER);
        }
    }

    /** 일일 코디는 특정 룩북에 속해 있지 않는 상태라는 것을 검증. */
    private void validateDailyCoordinate(Coordinate coordinate) {
        if (coordinate.getLookBook() != null) {
            throw new BaseCustomException(CoordinateErrorCode.NOT_DAILY_COORDINATE);
        }
    }

    private LookBook getLookBookById(Long lookBookId) {
        return lookBookRepository
                .findById(lookBookId)
                .orElseThrow(() -> new BaseCustomException(LookBookErrorCode.LOOK_BOOK_NOT_FOUND));
    }

    private Coordinate getCoordinateById(Long coordinateId) {
        return coordinateRepository
                .findById(coordinateId)
                .orElseThrow(
                        () -> new BaseCustomException(CoordinateErrorCode.COORDINATE_NOT_FOUND));
    }
}
