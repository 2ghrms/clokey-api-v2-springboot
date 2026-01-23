package org.clokey.domain.search.event;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.clokey.domain.history.repository.HistoryRepository;
import org.clokey.domain.search.repository.SearchRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberDeleteEventListener {

    private final SearchRepository searchRepository;
    private final HistoryRepository historyRepository;

    @TransactionalEventListener(
            classes = MemberDeleteEvent.class,
            phase = TransactionPhase.AFTER_COMMIT)
    public void handleMemberDelete(MemberDeleteEvent event) {
        try {
            Long memberId = event.memberId();

            // 1. Member가 가지고 있던 모든 History ID
            List<Long> historyIds = event.historyIds();

            // 2. HistoryDocument 삭제
            if (!historyIds.isEmpty()) {
                for (Long historyId : historyIds) {
                    try {
                        searchRepository.deleteHistory(historyId.toString());
                    } catch (Exception e) {
                        log.warn("[search] HistoryDocument 삭제 실패 - historyId: {}", historyId, e);
                    }
                }
                log.info(
                        "[search] Member의 HistoryDocument 삭제 완료 - memberId: {}, historyCount: {}",
                        memberId,
                        historyIds.size());
            }

            // 3. MemberDocument 삭제
            try {
                searchRepository.deleteMember(memberId.toString());
                log.info("MemberDocument 삭제 완료 - memberId: {}", memberId);
            } catch (Exception e) {
                log.error("MemberDocument 삭제 실패 - memberId: {}", memberId, e);
            }
        } catch (Exception e) {
            log.error("Member 삭제 이벤트 처리 실패 - memberId: {}", event.memberId(), e);
        }
    }
}
