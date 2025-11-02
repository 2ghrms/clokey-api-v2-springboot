package org.clokey.domain.lookbook.repository;

import org.clokey.domain.lookbook.dto.response.LookBookListResponse;
import org.clokey.global.paging.SortDirection;
import org.springframework.data.domain.Slice;

public interface LookBookRepositoryCustom {

    Slice<LookBookListResponse> findAllLookBookByMemberId(
            Long currentMemberId, Long lastLookBookId, int size, SortDirection direction);
}
