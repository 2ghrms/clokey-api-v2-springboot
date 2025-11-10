package org.clokey.domain.history.repository;

import java.util.List;
import org.clokey.history.entity.HistoryStyle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryStyleRepository
        extends JpaRepository<HistoryStyle, Long>, HistoryStyleRepositoryCustom {
    List<HistoryStyle> findByHistoryId(Long historyId);
}
