package org.clokey.domain.member.repository;

import java.util.Optional;
import org.clokey.member.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockRepository extends JpaRepository<Block, Long>, BlockRepositoryCustom {

    Optional<Block> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    boolean existsByBlockerIdAndBlockedIdOrBlockerIdAndBlockedId(
            Long fromId1, Long toId1, Long fromId2, Long toId2);
}
