package org.clokey.domain.category.repository;

import java.util.List;
import org.clokey.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    long countByIdIn(Iterable<Long> ids);

    List<Category> findAllById(Iterable<Long> ids);

    List<Category> findAllByParentIsNull();

    List<Category> findAllByParentId(Long parentId);
}
