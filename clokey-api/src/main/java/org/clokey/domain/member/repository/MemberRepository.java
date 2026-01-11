package org.clokey.domain.member.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.clokey.member.entity.Member;
import org.clokey.member.entity.OauthInfo;
import org.clokey.member.enums.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    boolean existsByClokeyId(String clokeyId);

    Optional<Member> findByOauthInfo(OauthInfo oauthInfo);

    Optional<Member> findByClokeyId(String clokeyId);

    @Query("SELECT m FROM Member m WHERE m.memberStatus = :status AND m.updatedAt < :cutoffDate")
    List<Member> findInactiveMembersBefore(
            @Param("status") MemberStatus status, @Param("cutoffDate") LocalDateTime cutoffDate);
}
