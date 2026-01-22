package org.clokey.domain.coordinate.service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.clokey.cloth.entity.Cloth;
import org.clokey.coordinate.entity.Coordinate;
import org.clokey.coordinate.entity.CoordinateCloth;
import org.clokey.coordinate.enums.CoordinateType;
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
import org.clokey.exception.BaseCustomException;
import org.clokey.global.paging.SortDirection;
import org.clokey.global.util.MemberUtil;
import org.clokey.lookbook.entity.LookBook;
import org.clokey.member.entity.Member;
import org.clokey.response.SliceResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Slice;
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

    private final ApplicationEventPublisher eventPublisher;

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

    @Override
    @Transactional
    public void updateCoordinate(Long coordinateId, CoordinateUpdateRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        final Coordinate coordinate = getCoordinateById(coordinateId);

        validateCoordinateOwner(coordinate, currentMember.getId());

        final Map<Long, Cloth> clothMap =
                clothRepository
                        .findAllById(
                                request.payloads().stream()
                                        .map(CoordinateUpdateRequest.Payload::clothId)
                                        .toList())
                        .stream()
                        .collect(Collectors.toMap(Cloth::getId, Function.identity()));

        validateAllClothesExist(
                request.payloads().stream().map(CoordinateUpdateRequest.Payload::clothId).toList(),
                clothMap);

        final List<Cloth> clothes =
                request.payloads().stream()
                        .map(payload -> clothMap.get(payload.clothId()))
                        .toList();

        validateSequentialOrders(
                request.payloads().stream()
                        .map(CoordinateUpdateRequest.Payload::order)
                        .sorted()
                        .toList());
        validateExceedingCoordinationClothesLimit(request.payloads());
        validateDuplicatedClothes(clothes);
        validateAllClothesOwnership(currentMember, clothes);

        /** Coordinate 업데이트 로직 */
        if (!Objects.equals(coordinate.getImageUrl(), request.coordinateImageUrl())) {
            eventPublisher.publishEvent(ImageDeleteEvent.of(coordinate.getImageUrl()));
        }
        coordinate.updateCoordinate(request.name(), request.memo(), request.coordinateImageUrl());

        /** CoordinateCloth 업데이트 로직 */
        List<CoordinateCloth> coordinateCloths = coordinate.getCoordinateClothes();

        Set<Long> requestedClothIds =
                clothes.stream().map(Cloth::getId).collect(Collectors.toSet());

        // 새로운 요청이 포함하지 않는 CoordinateCloth는 삭제한다.
        List<CoordinateCloth> toDelete =
                coordinateCloths.stream()
                        .filter(cc -> !requestedClothIds.contains(cc.getCloth().getId()))
                        .toList();

        coordinateClothRepository.deleteAllInBatch(toDelete);

        List<CoordinateCloth> toAdd = new ArrayList<>();

        for (CoordinateUpdateRequest.Payload payload : request.payloads()) {
            Cloth cloth = clothMap.get(payload.clothId());
            Optional<CoordinateCloth> existing =
                    coordinateCloths.stream()
                            .filter(cc -> cc.getCloth().getId().equals(payload.clothId()))
                            .findFirst();

            if (existing.isPresent()) {
                // 요청에도 포함되고 기존에도 존재하는 CoordinateCloth는 업데이트 한다.
                CoordinateCloth existingCloth = existing.get();
                existingCloth.updateCoordinateCloth(
                        payload.locationX(),
                        payload.locationY(),
                        payload.ratio(),
                        payload.degree(),
                        payload.order());
            } else {
                // 요청에 포함되고 기존에 존재하지 않던 CoordinateCloth는 생성한다.
                CoordinateCloth newCloth =
                        CoordinateCloth.createCoordinateCloth(
                                payload.locationX(),
                                payload.locationY(),
                                payload.ratio(),
                                payload.degree(),
                                payload.order(),
                                coordinate,
                                cloth);
                toAdd.add(newCloth);
            }
        }

        coordinateClothRepository.saveAll(toAdd);
    }

    @Override
    @Transactional
    public void deleteCoordinate(Long coordinateId) {
        final Member currentMember = memberUtil.getCurrentMember();
        final Coordinate coordinate = getCoordinateById(coordinateId);

        validateCoordinateOwner(coordinate, currentMember.getId());
        validateCoordinateInLookBook(coordinate);

        /** 일일 코디였던 경우, 통계값을 위해 데이터를 보존합니다. */
        if (coordinate.getCoordinateType() == CoordinateType.DAILY) {
            coordinate.detachDailyCoordinate();
            return;
        }

        coordinateClothRepository.deleteAllByCoordinateId(coordinate.getId());
        eventPublisher.publishEvent(ImageDeleteEvent.of(coordinate.getImageUrl()));
        coordinateRepository.delete(coordinate);
    }

    @Override
    public SliceResponse<DailyCoordinateListResponse> getDailyCoordinates(
            Long lastCoordinateId, int size, SortDirection direction) {
        final Member currentMember = memberUtil.getCurrentMember();

        Slice<DailyCoordinateListResponse> result =
                coordinateRepository.findAllDailyCoordinateByMemberId(
                        currentMember.getId(), lastCoordinateId, size, direction);

        return SliceResponse.from(result);
    }

    @Override
    public List<DailyCoordinateClothResponse> getTodayDailyCoordinateClothes() {
        final Member currentMember = memberUtil.getCurrentMember();
        Optional<Coordinate> coordinate =
                coordinateRepository.findDailyCoordinateByDateAndMemberId(
                        LocalDate.now(), currentMember.getId());

        if (coordinate.isEmpty()) {
            return List.of();
        }

        List<CoordinateDetailsListResponse> details =
                coordinateRepository.findAllCoordinateDetailsByCoordinateId(
                        coordinate.get().getId());

        if (details.isEmpty()) {
            return List.of();
        }

        return details.stream().map(DailyCoordinateClothResponse::from).toList();
    }

    @Override
    public CoordinatePreviewResponse getCoordinatePreview(Long coordinateId) {
        final Member currentMember = memberUtil.getCurrentMember();
        final Coordinate coordinate = getCoordinateById(coordinateId);

        validateCoordinateOwner(coordinate, currentMember.getId());
        validateCoordinateInLookBook(coordinate);

        return CoordinatePreviewResponse.from(coordinate);
    }

    @Override
    public List<CoordinateDetailsListResponse> getCoordinateDetails(Long coordinateId) {
        final Member currentMember = memberUtil.getCurrentMember();
        final Coordinate coordinate = getCoordinateById(coordinateId);

        validateCoordinateOwner(coordinate, currentMember.getId());
        validateCoordinateInLookBook(coordinate);

        return coordinateRepository.findAllCoordinateDetailsByCoordinateId(coordinate.getId());
    }

    @Override
    @Transactional
    public void toggleCoordinateLike(Long coordinateId) {
        final Member currentMember = memberUtil.getCurrentMember();
        final Coordinate coordinate = getCoordinateById(coordinateId);

        validateCoordinateOwner(coordinate, currentMember.getId());
        validateCoordinateInLookBook(coordinate);
        validateCoordinateLikeLimit(currentMember.getId(), coordinate);

        coordinate.toggleLike();
    }

    @Override
    public List<FavoriteCoordinateResponse> getFavoriteCoordinates() {
        final Member currentMember = memberUtil.getCurrentMember();

        List<Coordinate> favoriteCoordinates =
                coordinateRepository.findLikedCoordinatesByMemberId(currentMember.getId());

        if (favoriteCoordinates.isEmpty()) {
            return List.of();
        }

        return favoriteCoordinates.stream().map(FavoriteCoordinateResponse::from).toList();
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
        if (coordinateRepository.existsDailyCoordinateByDateAndMemberId(date, memberId)) {
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

    private void validateDailyCoordinate(Coordinate coordinate) {
        if (coordinate.getCoordinateType() != CoordinateType.DAILY) {
            throw new BaseCustomException(CoordinateErrorCode.NOT_DAILY_COORDINATE);
        }
    }

    private void validateCoordinateInLookBook(Coordinate coordinate) {
        if (coordinate.getLookBook() == null) {
            throw new BaseCustomException(CoordinateErrorCode.COORDINATE_NOT_IN_LOOK_BOOK);
        }
    }

    private void validateCoordinateLikeLimit(Long memberId, Coordinate coordinate) {

        if (coordinate.getLiked().equals(false)
                && coordinateRepository.countByMemberIdAndLikedTrue(memberId) >= 5) {
            throw new BaseCustomException(CoordinateErrorCode.COORDINATE_LIKE_LIMIT);
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
