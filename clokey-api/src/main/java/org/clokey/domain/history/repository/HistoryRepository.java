package org.clokey.domain.history.repository;

import java.util.Optional;
import org.clokey.history.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HistoryRepository extends JpaRepository<History, Long> {
    @Query("SELECT h FROM History h JOIN FETCH h.member m WHERE h.id = :id")
    Optional<History> findByIdWithMember(Long id);

    boolean existsByMemberId(Long memberId);
}
