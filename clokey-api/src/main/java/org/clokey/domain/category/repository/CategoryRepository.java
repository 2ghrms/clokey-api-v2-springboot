package org.clokey.domain.category.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.clokey.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    long countByIdIn(Iterable<Long> ids);

    List<Category> findAllById(Iterable<Long> ids);

    List<Category> findAllByParentIsNull();

    List<Category> findAllByParentId(Long parentId);

    @Query("select c from Category c left join fetch c.parent where c.id = :id")
    Optional<Category> findByIdWithParent(@Param("id") Long id);

    @Query("select c from Category c left join fetch c.parent where c.id in :ids")
    List<Category> findAllByIdWithParent(@Param("ids") Set<Long> ids);
}
