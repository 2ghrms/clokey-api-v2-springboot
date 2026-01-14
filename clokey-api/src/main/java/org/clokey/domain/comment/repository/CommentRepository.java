package org.clokey.domain.comment.repository;

import org.clokey.comment.entitiy.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {

    @Modifying
    @Query("delete from Comment c where c.comment.id = :parentId")
    void deleteReplies(Long parentId);

    long countByHistoryIdAndBannedFalse(Long historyId);

    @Modifying
    @Query("delete from Comment c where c.history.id = :historyId")
    void deleteAllByHistoryId(Long historyId);
}
