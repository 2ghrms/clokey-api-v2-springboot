package org.clokey.domain.comment.service;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.clokey.comment.entitiy.Comment;
import org.clokey.domain.comment.dto.request.CommentCreateRequest;
import org.clokey.domain.comment.dto.response.CommentCreateResponse;
import org.clokey.domain.comment.dto.response.CommentListResponse;
import org.clokey.domain.comment.dto.response.MyCommentListResponse;
import org.clokey.domain.comment.dto.response.ReplyListResponse;
import org.clokey.domain.comment.event.NewCommentEvent;
import org.clokey.domain.comment.event.NewReplyEvent;
import org.clokey.domain.comment.exception.CommentErrorCode;
import org.clokey.domain.comment.repository.CommentRepository;
import org.clokey.domain.history.exception.HistoryErrorCode;
import org.clokey.domain.history.repository.HistoryRepository;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.paging.SortDirection;
import org.clokey.global.util.MemberUtil;
import org.clokey.history.entity.History;
import org.clokey.member.entity.Member;
import org.clokey.member.enums.Visibility;
import org.clokey.response.SliceResponse;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public CommentCreateResponse createComment(CommentCreateRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        final History history = getHistoryById(request.historyId());

        validateHistoryAuthority(currentMember, history);

        Comment comment = Comment.createParentComment(request.content(), currentMember, history);

        try {
            commentRepository.save(comment);
        } catch (DataIntegrityViolationException e) {
            String message = e.getMostSpecificCause().getMessage();

            if (message != null && message.contains("fk_comment_history")) {
                throw new BaseCustomException(HistoryErrorCode.HISTORY_NOT_FOUND);
            }

            throw e;
        }

        eventPublisher.publishEvent(
                new NewCommentEvent(
                        comment.getId(),
                        history.getId(),
                        history.getMember().getId(),
                        currentMember.getId(),
                        currentMember.getNickname(),
                        currentMember.getProfileImageUrl(),
                        comment.getContent()));

        return CommentCreateResponse.from(comment);
    }

    @Override
    @Transactional
    public CommentCreateResponse createReply(Long commentId, CommentCreateRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        final History history = getHistoryById(request.historyId());
        final Comment comment = getCommentById(commentId);

        validateHistoryAuthority(currentMember, comment.getHistory());
        validateParentCommentHistory(comment, history);
        validateReplyDepth(comment);

        Comment reply = Comment.createReply(request.content(), currentMember, history, comment);

        try {
            commentRepository.save(reply);
        } catch (DataIntegrityViolationException e) {
            String message = e.getMostSpecificCause().getMessage();

            if (message != null && message.contains("fk_comment_parent")) {
                throw new BaseCustomException(CommentErrorCode.COMMENT_NOT_FOUND);
            }

            if (message != null && message.contains("fk_comment_history")) {
                throw new BaseCustomException(HistoryErrorCode.HISTORY_NOT_FOUND);
            }

            throw e;
        }

        eventPublisher.publishEvent(
                new NewReplyEvent(
                        reply.getId(),
                        history.getId(),
                        commentId,
                        comment.getMember().getId(),
                        currentMember.getId(),
                        currentMember.getNickname(),
                        currentMember.getProfileImageUrl(),
                        request.content()));

        return CommentCreateResponse.from(comment);
    }

    @Override
    public SliceResponse<CommentListResponse> getHistoryComments(
            Long historyId, Long lastCommentId, int size, SortDirection direction) {
        final Member currentMember = memberUtil.getCurrentMember();
        final History history = getHistoryById(historyId);

        validateHistoryAuthority(currentMember, history);

        Slice<CommentListResponse> result =
                commentRepository.findAllParentCommentByHistoryId(
                        historyId, currentMember.getId(), lastCommentId, size, direction);
        return SliceResponse.from(result);
    }

    @Override
    public SliceResponse<ReplyListResponse> getCommentReplies(
            Long commentId, Long lastReplyId, int size, SortDirection direction) {
        final Member currentMember = memberUtil.getCurrentMember();
        final Comment comment = getCommentById(commentId);

        validateHistoryAuthority(currentMember, comment.getHistory());

        Slice<ReplyListResponse> result =
                commentRepository.findAllRepliesByCommentId(
                        commentId, currentMember.getId(), lastReplyId, size, direction);

        return SliceResponse.from(result);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        final Member currentMember = memberUtil.getCurrentMember();
        final Comment comment = getCommentById(commentId);

        validateCommentOwner(currentMember, comment);

        if (comment.getComment() != null) {
            commentRepository.delete(comment);
            return;
        }

        commentRepository.deleteReplies(comment.getId());
        commentRepository.delete(comment);
    }

    @Override
    public SliceResponse<MyCommentListResponse> getMyComments(
            Long lastHistoryId, int size, SortDirection direction) {
        final Member currentMember = memberUtil.getCurrentMember();

        Slice<MyCommentListResponse> result =
                commentRepository.findAllMyComments(
                        currentMember.getId(), lastHistoryId, size, direction);
        return SliceResponse.from(result);
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

    private void validateCommentOwner(Member member, Comment comment) {
        if (!Objects.equals(comment.getMember().getId(), member.getId())) {
            throw new BaseCustomException(CommentErrorCode.NOT_MY_COMMENT);
        }
    }

    private void validateReplyDepth(Comment parentComment) {
        if (parentComment.getComment() != null) {
            throw new BaseCustomException(CommentErrorCode.REPLY_ON_REPLY);
        }
    }

    private void validateParentCommentHistory(Comment parentComment, History history) {
        if (!Objects.equals(parentComment.getHistory().getId(), history.getId())) {
            throw new BaseCustomException(CommentErrorCode.REPLY_HISTORY_PARENT_HISTORY_MISMATCH);
        }
    }
}
