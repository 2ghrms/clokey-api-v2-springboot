package org.clokey.domain.like.repository;

import java.util.List;
import java.util.Optional;
import org.clokey.like.entity.MemberLike;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MemberLikeRepository extends JpaRepository<MemberLike, Long> {

    long countByHistoryId(Long historyId);

    @Query(
            """
        SELECT ml
        FROM MemberLike ml
        WHERE ml.member.id = :memberId
          AND (:lastLikeId IS NULL OR ml.id < :lastLikeId)
        ORDER BY ml.id DESC
        """)
    List<MemberLike> findLikedHistoriesByMemberId(
            Long memberId, Long lastLikeId, Pageable pageable);

    @Query(
            """
        SELECT ml
        FROM MemberLike ml
        WHERE ml.history.id = :historyId
          AND (:lastLikeId IS NULL OR ml.id < :lastLikeId)
        ORDER BY ml.id DESC
        """)
    List<MemberLike> findLikeMembersByHistoryId(Long historyId, Long lastLikeId, Pageable pageable);

    Optional<MemberLike> findByMemberIdAndHistoryId(Long memberId, Long historyId);
}
