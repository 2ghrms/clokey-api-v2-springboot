package org.clokey.domain.history.repository;

import java.util.List;
import org.clokey.history.entity.HistoryClothTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryClothTagRepository
        extends JpaRepository<HistoryClothTag, Long>, HistoryClothTagRepositoryCustom {
    List<HistoryClothTag> findByHistoryImageId(Long historyImageId);
}
