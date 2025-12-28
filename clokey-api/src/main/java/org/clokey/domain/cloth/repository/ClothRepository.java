package org.clokey.domain.cloth.repository;

import org.clokey.cloth.entity.Cloth;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClothRepository extends JpaRepository<Cloth, Long>, ClothRepositoryCustom {
    boolean existsByMemberId(Long memberId);
}
