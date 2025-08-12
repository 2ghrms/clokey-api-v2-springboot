package org.clokey.domain.comment.service;

import org.clokey.domain.comment.dto.request.CommentCreateRequest;
import org.clokey.domain.comment.dto.request.ReplyCreateRequest;
import org.clokey.domain.comment.dto.response.CommentCreateResponse;
import org.clokey.domain.comment.dto.response.ReplyCreateResponse;

public interface CommentService {

    CommentCreateResponse createComment(CommentCreateRequest request);

    ReplyCreateResponse createReply(Long commentId, ReplyCreateRequest request);
}
