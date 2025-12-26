package org.clokey.domain.term.repository;

import java.util.List;
import java.util.Optional;
import org.clokey.term.entity.MemberTerm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberTermRepository extends JpaRepository<MemberTerm, Long> {

    boolean existsByMemberId(Long memberId);

    List<MemberTerm> findByMemberIdAndTermIdIn(Long memberId, List<Long> termIds);

    Optional<MemberTerm> findByMemberIdAndTermId(Long memberId, Long termId);
}
