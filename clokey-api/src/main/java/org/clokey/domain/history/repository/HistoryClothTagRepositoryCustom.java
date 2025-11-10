package org.clokey.domain.history.repository;

import java.util.List;
import org.clokey.history.entity.HistoryClothTag;

public interface HistoryClothTagRepositoryCustom {
    void bulkInsertHistoryClothTags(List<HistoryClothTag> tags);
}
