package org.clokey.domain.history.repository;

import org.clokey.history.entity.Situation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SituationRepository
        extends JpaRepository<Situation, Long>, SituationRepositoryCustom {}
