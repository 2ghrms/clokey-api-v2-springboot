package org.clokey.domain.history.repository;

import org.clokey.history.entity.HashtagHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HashtagHistoryRepository extends JpaRepository<HashtagHistory, Long> {}
