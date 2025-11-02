package org.clokey.domain.coordinate.repository;

import java.util.List;
import org.clokey.coordinate.entity.CoordinateCloth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CoordinateClothRepository extends JpaRepository<CoordinateCloth, Long> {

    @Modifying
    @Query("DELETE FROM CoordinateCloth cc WHERE cc.coordinate.id = :coordinateId")
    void deleteAllByCoordinateId(Long coordinateId);

    @Modifying
    @Query("DELETE FROM CoordinateCloth cc WHERE cc.coordinate.id IN :coordinateIds")
    void deleteAllByCoordinateIds(List<Long> coordinateIds);
}
