package org.clokey.domain.comment.repository;

import org.clokey.domain.comment.dto.response.CommentListResponse;
import org.clokey.global.paging.SortDirection;
import org.springframework.data.domain.Slice;

public interface CommentRepositoryCustom {
    Slice<CommentListResponse> findAllByHistoryId(
            Long historyId, Long lastHistoryId, int size, SortDirection direction);
}
