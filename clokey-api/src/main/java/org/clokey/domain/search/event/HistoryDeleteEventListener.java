package org.clokey.domain.search.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.clokey.domain.search.repository.SearchRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class HistoryDeleteEventListener {

    private final SearchRepository searchRepository;

    @TransactionalEventListener(
            classes = HistoryDeleteEvent.class,
            phase = TransactionPhase.AFTER_COMMIT)
    public void handleHistoryDelete(HistoryDeleteEvent event) {
        try {
            searchRepository.deleteHistory(event.historyId().toString());
            log.info("[search] HistoryDocument 삭제 완료 - historyId: {}", event.historyId());
        } catch (Exception e) {
            log.error("[search] HistoryDocument 삭제 실패 - historyId: {}", event.historyId(), e);
        }
    }
}
