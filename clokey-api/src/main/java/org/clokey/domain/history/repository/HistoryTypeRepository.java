package org.clokey.domain.history.repository;

import org.clokey.history.entity.HistoryType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryTypeRepository extends JpaRepository<HistoryType, Long> {}
