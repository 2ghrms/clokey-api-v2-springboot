package org.clokey.domain.comment.repository;

import org.clokey.domain.comment.dto.response.ReplyListResponse;
import org.clokey.global.paging.SortDirection;
import org.springframework.data.domain.Slice;

public interface ReplyRepositoryCustom {
    Slice<ReplyListResponse> findAllByCommentId(
            Long commentId,
            Long currentMemberId,
            Long lastReplyId,
            int size,
            SortDirection direction);
}
