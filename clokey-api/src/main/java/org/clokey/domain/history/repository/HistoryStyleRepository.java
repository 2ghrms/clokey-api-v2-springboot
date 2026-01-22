package org.clokey.domain.history.repository;

import java.util.List;
import org.clokey.history.entity.HistoryStyle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HistoryStyleRepository
        extends JpaRepository<HistoryStyle, Long>, HistoryStyleRepositoryCustom {
    List<HistoryStyle> findByHistoryId(Long historyId);

    @Query(
            """
        select new org.clokey.domain.history.repository.HistoryStyleRepository$HistoryStyleInfo(
            hs.history.id, hs.style.id, hs.style.name
        )
        from HistoryStyle hs
        where hs.history.id in :historyIds
    """)
    List<HistoryStyleInfo> findStyleInfoByHistoryIds(List<Long> historyIds);

    record HistoryStyleInfo(Long historyId, Long styleId, String styleName) {}
}
