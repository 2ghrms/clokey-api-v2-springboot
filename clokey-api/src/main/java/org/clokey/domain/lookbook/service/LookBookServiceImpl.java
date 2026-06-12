package org.clokey.domain.lookbook.service;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.clokey.coordinate.entity.Coordinate;
import org.clokey.coordinate.enums.CoordinateType;
import org.clokey.domain.coordinate.repository.CoordinateClothRepository;
import org.clokey.domain.coordinate.repository.CoordinateRepository;
import org.clokey.domain.image.event.ImagesDeleteEvent;
import org.clokey.domain.lookbook.dto.request.LookBookCreateRequest;
import org.clokey.domain.lookbook.dto.request.LookBookUpdateRequest;
import org.clokey.domain.lookbook.dto.response.CoordinateListResponse;
import org.clokey.domain.lookbook.dto.response.LookBookCreateResponse;
import org.clokey.domain.lookbook.dto.response.LookBookListResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LookBookServiceImpl implements LookBookService {

    private final LookBookRepository lookBookRepository;
    private final CoordinateRepository coordinateRepository;
    private final CoordinateClothRepository coordinateClothRepository;

    private final MemberUtil memberUtil;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public LookBookCreateResponse createLookBook(LookBookCreateRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();

        LookBook lookBook = LookBook.createLookBook(request.name(), currentMember);
        lookBookRepository.save(lookBook);

        return LookBookCreateResponse.from(lookBook);
    }

    @Override
    @Transactional
    public void updateLookBook(Long lookBookId, LookBookUpdateRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        final LookBook lookBook = getLookBookById(lookBookId);

        validateLookBookOwner(lookBook, currentMember.getId());
        lookBook.updateLookBook(request.name());
    }

    @Override
    @Transactional
    public void deleteLookBook(Long lookBookId) {
        final Member currentMember = memberUtil.getCurrentMember();
        final LookBook lookBook = getLookBookById(lookBookId);

        validateLookBookOwner(lookBook, currentMember.getId());

        List<Coordinate> coordinates = coordinateRepository.findAllByLookBookId(lookBook.getId());

        /** 일일 코디였던 경우, 통계값을 위해 데이터를 보존합니다. */
        coordinates.stream()
                .filter(c -> c.getCoordinateType() == CoordinateType.DAILY)
                .forEach(Coordinate::detachDailyCoordinate);

        List<Coordinate> defaultCoordinates =
                coordinates.stream()
                        .filter(c -> c.getCoordinateType() != CoordinateType.DAILY)
                        .toList();

        coordinateClothRepository.deleteAllByCoordinateIds(
                defaultCoordinates.stream().map(Coordinate::getId).toList());
        eventPublisher.publishEvent(
                ImagesDeleteEvent.of(
                        defaultCoordinates.stream().map(Coordinate::getImageUrl).toList()));
        coordinateRepository.deleteAll(defaultCoordinates);

        lookBookRepository.delete(lookBook);
    }

    @Override
    public SliceResponse<LookBookListResponse> getLookBooks(
            Long lastLookBookId, int size, SortDirection direction) {
        final Member currentMember = memberUtil.getCurrentMember();

        Slice<LookBookListResponse> result =
                lookBookRepository.findAllLookBookByMemberId(
                        currentMember.getId(), lastLookBookId, size, direction);

        return SliceResponse.from(result);
    }

    @Override
    public SliceResponse<CoordinateListResponse> getCoordinates(
            Long lookBookId, Long lastCoordinateId, int size, SortDirection direction) {
        final Member currentMember = memberUtil.getCurrentMember();
        final LookBook lookBook = getLookBookById(lookBookId);

        validateLookBookOwner(lookBook, currentMember.getId());

        Slice<CoordinateListResponse> result =
                coordinateRepository.findAllCoordinateByLookBookId(
                        lookBook.getId(), lastCoordinateId, size, direction);

        return SliceResponse.from(result);
    }

    private void validateLookBookOwner(LookBook lookBook, Long memberId) {
        if (!Objects.equals(lookBook.getMember().getId(), memberId)) {
            throw new BaseCustomException(LookBookErrorCode.NOT_LOOK_BOOK_OWNER);
        }
    }

    private LookBook getLookBookById(Long lookBookId) {
        return lookBookRepository
                .findById(lookBookId)
                .orElseThrow(() -> new BaseCustomException(LookBookErrorCode.LOOK_BOOK_NOT_FOUND));
    }
}
