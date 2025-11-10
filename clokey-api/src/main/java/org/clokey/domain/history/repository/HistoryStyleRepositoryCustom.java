package org.clokey.domain.history.repository;

import java.util.List;
import org.clokey.history.entity.HistoryStyle;

public interface HistoryStyleRepositoryCustom {
    void bulkInsertHistoryStyles(List<HistoryStyle> styles);
}
