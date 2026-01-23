package org.clokey.domain.search.event;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.clokey.domain.search.document.HistoryDocument;
import org.clokey.domain.search.document.MemberDocument;
import org.clokey.domain.search.repository.SearchRepository;
import org.clokey.domain.search.service.SearchDocumentServiceImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeiliSearchSyncEventListener {

    private final SearchRepository searchRepository;
    private final SearchDocumentServiceImpl documentService;

    @Async
    @TransactionalEventListener(
            classes = MeiliSearchSyncEvent.class,
            phase = TransactionPhase.AFTER_COMMIT)
    public void handleMeiliSearchSync(MeiliSearchSyncEvent event) {
        try {
            if (event.entityType() == MeiliSearchSyncEvent.EntityType.HISTORY) {
                HistoryDocument document = documentService.toHistoryDocument(event.entityId());

                searchRepository.saveAllHistories(List.of(document));

                log.debug("[search] HistoryDocument 동기화 완료 - historyId: {}", event.entityId());
            } else if (event.entityType() == MeiliSearchSyncEvent.EntityType.MEMBER) {
                MemberDocument document = documentService.toMemberDocument(event.entityId());

                searchRepository.saveAllMembers(List.of(document));

                log.debug("[search] MemberDocument 동기화 완료 - memberId: {}", event.entityId());
            }
        } catch (IllegalArgumentException e) {
            log.debug(
                    "[search] 엔티티가 존재하지 않음 - entityType: {}, entityId: {}",
                    event.entityType(),
                    event.entityId());
        } catch (Exception e) {
            log.error(
                    "[search] MeiliSearch 동기화 실패 - entityType: {}, entityId: {}",
                    event.entityType(),
                    event.entityId(),
                    e);
        }
    }
}
