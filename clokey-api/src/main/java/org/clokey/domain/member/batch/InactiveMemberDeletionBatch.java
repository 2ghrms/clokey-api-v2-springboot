package org.clokey.domain.member.batch;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.clokey.domain.auth.service.AuthService;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.member.entity.Member;
import org.clokey.member.enums.MemberStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class InactiveMemberDeletionBatch {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final MemberRepository memberRepository;
    private final AuthService authService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul") // 매일 00:00:00 KST
    @Transactional
    public void deleteInactiveMembers() {
        LocalDateTime cutoffDate = LocalDateTime.now(KST).minusDays(15);
        List<Member> inactiveMembers =
                memberRepository.findInactiveMembersBefore(MemberStatus.INACTIVE, cutoffDate);

        log.info("삭제 대상 INACTIVE 회원 수: {}", inactiveMembers.size());

        for (Member member : inactiveMembers) {
            try {
                authService.withdrawMemberById(member.getId());
            } catch (Exception e) {
                log.error("회원 탈퇴 실패 - memberId: {}", member.getId(), e);
            }
        }

        log.info("INACTIVE 회원 {}명 삭제 완료", inactiveMembers.size());
    }
}
