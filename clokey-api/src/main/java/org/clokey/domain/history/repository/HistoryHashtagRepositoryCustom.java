package org.clokey.domain.history.repository;

import java.util.List;
import org.clokey.history.entity.HistoryHashtag;

public interface HistoryHashtagRepositoryCustom {
    void bulkInsertHistoryHashtags(List<HistoryHashtag> links);
}
