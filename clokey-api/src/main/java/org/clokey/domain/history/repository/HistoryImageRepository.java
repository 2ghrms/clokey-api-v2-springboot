package org.clokey.domain.history.repository;

import java.util.List;
import org.clokey.history.entity.HistoryImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryImageRepository extends JpaRepository<HistoryImage, Long> {
    List<HistoryImage> findByHistoryId(Long historyId);
}
