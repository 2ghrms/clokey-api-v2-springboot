package org.clokey.domain.like.repository;

import org.clokey.like.entity.MemberLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberLikeRepository extends JpaRepository<MemberLike, Long> {}
