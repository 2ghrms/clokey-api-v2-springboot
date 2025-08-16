package org.clokey.domain.term.repository;

import org.clokey.term.entity.MemberTerm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberTermRepository extends JpaRepository<MemberTerm, Long> {

    boolean existsByMemberId(Long memberId);
}
