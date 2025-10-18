package org.clokey.domain.member.repository;

import java.util.Optional;
import org.clokey.member.entity.Member;
import org.clokey.member.entity.OauthInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByClokeyId(String clokeyId);

    Optional<Member> findByOauthInfo(OauthInfo oauthInfo);

    Optional<Member> findByClokeyId(String clokeyId);
}
