package org.clokey.domain.lookbook.service;

import org.clokey.domain.lookbook.dto.request.LookBookCreateRequest;
import org.clokey.domain.lookbook.dto.request.LookBookUpdateRequest;
import org.clokey.domain.lookbook.dto.response.CoordinateListResponse;
import org.clokey.domain.lookbook.dto.response.LookBookCreateResponse;
import org.clokey.domain.lookbook.dto.response.LookBookListResponse;
import org.clokey.global.paging.SortDirection;
import org.clokey.response.SliceResponse;

public interface LookBookService {

    LookBookCreateResponse createLookBook(LookBookCreateRequest request);

    void updateLookBook(Long lookBookId, LookBookUpdateRequest request);

    void deleteLookBook(Long lookBookId);

    SliceResponse<LookBookListResponse> getLookBooks(
            Long lastLookBookId, int size, SortDirection direction);

    SliceResponse<CoordinateListResponse> getCoordinates(
            Long lookBookId, Long lastCoordinateId, int size, SortDirection direction);
}
