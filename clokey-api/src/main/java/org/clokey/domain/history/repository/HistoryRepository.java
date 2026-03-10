package org.clokey.domain.history.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.clokey.history.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HistoryRepository extends JpaRepository<History, Long> {
    @Query("SELECT h FROM History h JOIN FETCH h.member m WHERE h.id = :id")
    Optional<History> findByIdWithMember(Long id);

    boolean existsByMemberId(Long memberId);

    boolean existsByMemberIdAndHistoryDate(Long memberId, LocalDate historyDate);

    @Query(
            """
            select h from History h
            where h.member.id = :memberId
            and FUNCTION('YEAR', h.historyDate) = :year
            and FUNCTION('MONTH', h.historyDate) = :month
            and h.banned = false
            order by h.historyDate asc
            """)
    List<History> findByMemberIdAndYearAndMonthNotBanned(Long memberId, int year, int month);

    @Query("SELECT h.id FROM History h WHERE h.member.id = :memberId")
    List<Long> findAllIdsByMemberId(Long memberId);

    @Query("SELECT h.id FROM History h")
    List<Long> findAllIds();

    @Deprecated
    @Query(
            """
        select new org.clokey.domain.history.repository.HistoryRepository$HistorySituationInfo(
            h.id, s.id, s.name
        )
        from History h
        join h.situation s
        where h.id in :historyIds
    """)
    List<HistorySituationInfo> findSituationInfoByHistoryIds(List<Long> historyIds);

    record HistorySituationInfo(Long historyId, Long situationId, String situationName) {}

    @Query(
            """
            SELECT h.id FROM History h
            JOIN h.member m
            WHERE h.id IN :historyIds
            AND (h.banned = true OR m.memberStatus = 'BANNED')
            """)
    Set<Long> findBannedHistoryIdsAmong(List<Long> historyIds);
}
