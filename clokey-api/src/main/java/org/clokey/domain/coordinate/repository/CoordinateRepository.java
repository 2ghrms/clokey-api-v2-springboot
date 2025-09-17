package org.clokey.domain.coordinate.repository;

import org.clokey.coordinate.entity.Coordinate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoordinateRepository extends JpaRepository<Coordinate, Long> {}
