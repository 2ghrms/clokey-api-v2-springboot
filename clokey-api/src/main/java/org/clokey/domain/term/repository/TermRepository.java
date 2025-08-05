package org.clokey.domain.term.repository;

import org.clokey.term.entity.Term;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermRepository extends JpaRepository<Term, Long> {}
