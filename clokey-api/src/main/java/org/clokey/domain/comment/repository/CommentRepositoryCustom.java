package org.clokey.domain.comment.repository;

import org.clokey.domain.comment.dto.response.CommentListResponse;
import org.clokey.domain.comment.dto.response.MyCommentListResponse;
import org.clokey.domain.comment.dto.response.ReplyListResponse;
import org.clokey.global.paging.SortDirection;
import org.springframework.data.domain.Slice;

public interface CommentRepositoryCustom {
    Slice<CommentListResponse> findAllParentCommentByHistoryId(
            Long historyId,
            Long currentMemberId,
            Long lastHistoryId,
            int size,
            SortDirection direction);

    Slice<ReplyListResponse> findAllRepliesByCommentId(
            Long commentId,
            Long currentMemberId,
            Long lastReplyId,
            int size,
            SortDirection direction);

    Slice<MyCommentListResponse> findAllMyComments(
            Long myMemberId, Long lastHistoryId, int size, SortDirection direction);
}
