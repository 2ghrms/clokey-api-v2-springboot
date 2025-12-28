package org.clokey.domain.history.repository;

import org.clokey.history.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepository extends JpaRepository<History, Long> {
    boolean existsByMemberId(Long memberId);
}
