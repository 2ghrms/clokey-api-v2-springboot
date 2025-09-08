package org.clokey.domain.comment.service;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.clokey.comment.entitiy.Comment;
import org.clokey.comment.entitiy.Reply;
import org.clokey.domain.comment.dto.request.CommentCreateRequest;
import org.clokey.domain.comment.dto.request.ReplyCreateRequest;
import org.clokey.domain.comment.dto.response.CommentCreateResponse;
import org.clokey.domain.comment.dto.response.CommentListResponse;
import org.clokey.domain.comment.dto.response.ReplyCreateResponse;
import org.clokey.domain.comment.dto.response.ReplyListResponse;
import org.clokey.domain.comment.exception.CommentErrorCode;
import org.clokey.domain.comment.repository.CommentRepository;
import org.clokey.domain.comment.repository.ReplyRepository;
import org.clokey.domain.history.exception.HistoryErrorCode;
import org.clokey.domain.history.repository.HistoryRepository;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.paging.SortDirection;
import org.clokey.global.util.MemberUtil;
import org.clokey.history.entity.History;
import org.clokey.member.entity.Member;
import org.clokey.member.enums.Visibility;
import org.clokey.response.SliceResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final MemberUtil memberUtil;

    private final CommentRepository commentRepository;
    private final HistoryRepository historyRepository;
    private final ReplyRepository replyRepository;

    @Override
    @Transactional
    public CommentCreateResponse createComment(CommentCreateRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        final History history = getHistoryById(request.historyId());

        validateHistoryAuthority(currentMember, history);

        Comment comment = Comment.createComment(request.content(), currentMember, history);

        try {
            commentRepository.save(comment);
        } catch (DataIntegrityViolationException e) {
            String message = e.getMostSpecificCause().getMessage();

            if (message != null && message.contains("fk_comment_history")) {
                throw new BaseCustomException(CommentErrorCode.COMMENT_NOT_FOUND);
            }

            throw e;
        }

        return CommentCreateResponse.from(comment);
    }

    @Override
    @Transactional
    public ReplyCreateResponse createReply(Long commentId, ReplyCreateRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        final Comment comment = getCommentById(commentId);

        validateHistoryAuthority(currentMember, comment.getHistory());

        Reply reply = Reply.createReply(request.content(), currentMember, comment);

        try {
            replyRepository.save(reply);
        } catch (DataIntegrityViolationException e) {
            String message = e.getMostSpecificCause().getMessage();

            if (message != null && message.contains("fk_reply_comment")) {
                throw new BaseCustomException(CommentErrorCode.COMMENT_NOT_FOUND);
            }

            throw e;
        }

        return ReplyCreateResponse.from(reply);
    }

    @Override
    public SliceResponse<CommentListResponse> getHistoryComments(
            Long historyId, Long lastCommentId, int size, SortDirection direction) {
        final Member currentMember = memberUtil.getCurrentMember();
        final History history = getHistoryById(historyId);

        validateHistoryAuthority(currentMember, history);

        Slice<CommentListResponse> result =
                commentRepository.findAllByHistoryId(historyId, lastCommentId, size, direction);
        return SliceResponse.from(result);
    }

    @Override
    public SliceResponse<ReplyListResponse> getCommentReplies(
            Long commentId, Long lastReplyId, int size, SortDirection direction) {
        final Member currentMember = memberUtil.getCurrentMember();
        final Comment comment = getCommentById(commentId);

        validateHistoryAuthority(currentMember, comment.getHistory());

        Slice<ReplyListResponse> result =
                replyRepository.findAllByCommentId(commentId, lastReplyId, size, direction);

        return SliceResponse.from(result);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        final Member currentMember = memberUtil.getCurrentMember();
        final Comment comment = getCommentById(commentId);

        validateCommentOwner(currentMember, comment);

        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public void deleteReply(Long commentId, Long replyId) {
        final Member currentMember = memberUtil.getCurrentMember();
        final Reply reply = getReplyById(replyId);

        validateReplyOwner(currentMember, reply);
        validateReplyFromComment(reply, commentId);

        replyRepository.delete(reply);
    }

    private void validateHistoryAuthority(Member member, History history) {
        if (history.getMember().getVisibility() == Visibility.PRIVATE
                && !history.getMember().getId().equals(member.getId())) {
            throw new BaseCustomException(HistoryErrorCode.LIMITED_AUTHORITY);
        }
    }

    private History getHistoryById(Long historyId) {
        return historyRepository
                .findById(historyId)
                .orElseThrow(() -> new BaseCustomException(HistoryErrorCode.HISTORY_NOT_FOUND));
    }

    private Comment getCommentById(Long commentId) {
        return commentRepository
                .findById(commentId)
                .orElseThrow(() -> new BaseCustomException(CommentErrorCode.COMMENT_NOT_FOUND));
    }

    private Reply getReplyById(Long replyId) {
        return replyRepository
                .findById(replyId)
                .orElseThrow(() -> new BaseCustomException(CommentErrorCode.REPLY_NOT_FOUND));
    }

    private void validateCommentOwner(Member member, Comment comment) {
        if (!Objects.equals(comment.getMember().getId(), member.getId())) {
            throw new BaseCustomException(CommentErrorCode.NOT_MY_COMMENT);
        }
    }

    private void validateReplyOwner(Member member, Reply reply) {
        if (!Objects.equals(reply.getMember().getId(), member.getId())) {
            throw new BaseCustomException(CommentErrorCode.NOT_MY_REPLY);
        }
    }

    private void validateReplyFromComment(Reply reply, Long commentId) {
        if (!Objects.equals(reply.getComment().getId(), commentId)) {
            throw new BaseCustomException(CommentErrorCode.REPLY_NOT_FROM_COMMENT);
        }
    }
}
