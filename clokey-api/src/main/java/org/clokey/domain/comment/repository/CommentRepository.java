package org.clokey.domain.comment.repository;

import org.clokey.comment.entitiy.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {}
