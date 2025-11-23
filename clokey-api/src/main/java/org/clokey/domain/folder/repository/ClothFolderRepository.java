package org.clokey.domain.folder.repository;

import java.util.List;
import org.clokey.folder.entity.ClothFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ClothFolderRepository extends JpaRepository<ClothFolder, Long> {

    List<ClothFolder> findAllByClothId(Long clothId);

    @Modifying
    @Query("DELETE FROM ClothFolder cf WHERE cf.cloth.id = :clothId")
    void deleteAllByClothId(Long clothId);
}
