package org.clokey.domain.comment.repository;

import org.clokey.comment.entitiy.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReplyRepository extends JpaRepository<Reply, Long>, ReplyRepositoryCustom {

    @Modifying
    @Query("delete from Reply r where r.comment.id = :commentId")
    void deleteByCommentId(@Param("commentId") Long commentId);
}
