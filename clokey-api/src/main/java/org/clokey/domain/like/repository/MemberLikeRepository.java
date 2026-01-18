package org.clokey.domain.like.repository;

import java.util.List;
import java.util.Optional;
import org.clokey.like.entity.MemberLike;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Modifying
    @Query("delete from MemberLike ml where ml.history.id = :historyId")
    void deleteAllByHistoryId(Long historyId);

    @Query(
            """
        select ml.history.id
        from MemberLike ml
        where ml.member.id = :memberId
          and ml.history.id in :historyIds
    """)
    List<Long> findLikedHistoryIds(Long memberId, List<Long> historyIds);
}
