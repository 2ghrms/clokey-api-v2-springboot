package org.clokey.domain.history.repository;

import java.util.List;
import org.clokey.history.entity.HistoryClothTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface HistoryClothTagRepository
        extends JpaRepository<HistoryClothTag, Long>, HistoryClothTagRepositoryCustom {
    List<HistoryClothTag> findByHistoryImageId(Long historyImageId);

    List<HistoryClothTag> findAllByClothId(Long clothId);

    @Modifying
    @Query("DELETE FROM HistoryClothTag hct WHERE hct.cloth.id = :clothId")
    void deleteAllByClothId(Long clothId);

    @Query(
            "select hct from HistoryClothTag hct "
                    + "join fetch hct.cloth "
                    + "where hct.historyImage.id = :historyImageId")
    List<HistoryClothTag> findAllByHistoryImageIdWithCloth(Long historyImageId);
}
