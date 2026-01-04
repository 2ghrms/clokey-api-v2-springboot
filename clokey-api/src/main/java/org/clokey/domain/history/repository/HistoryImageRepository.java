package org.clokey.domain.history.repository;

import java.util.List;
import org.clokey.history.entity.HistoryImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HistoryImageRepository extends JpaRepository<HistoryImage, Long> {
    @Query(
            """
        SELECT hi.history.id, hi.imageUrl
        FROM HistoryImage hi
        WHERE hi.history.id IN :historyIds
        AND hi.id = (
            SELECT MIN(h2.id)
            FROM HistoryImage h2
            WHERE h2.history.id = hi.history.id
        )
    """)
    List<Object[]> getFirstImageUrlsWithHistoryId(@Param("historyIds") List<Long> historyIds);

    List<HistoryImage> findByHistoryId(Long historyId);
}
