package org.clokey.domain.member.repository;

import org.clokey.domain.member.dto.response.BlockedMemberResponse;
import org.clokey.global.paging.SortDirection;
import org.springframework.data.domain.Slice;

public interface BlockRepositoryCustom {

    Slice<BlockedMemberResponse> findAllByBlockerId(
            Long blockerId, Long lastBlockId, Integer size, SortDirection direction);
}
