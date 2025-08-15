package org.clokey.domain.comment.repository;

import org.clokey.comment.entitiy.Reply;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplyRepository extends JpaRepository<Reply, Long>, ReplyRepositoryCustom {}
