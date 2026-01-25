package org.clokey.domain.search.event;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.clokey.domain.search.document.HistoryDocument;
import org.clokey.domain.search.repository.SearchRepository;
import org.clokey.domain.search.service.SearchDocumentServiceImpl;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("prod")
public class ClothDeleteEventListener {

    private final SearchRepository searchRepository;
    private final SearchDocumentServiceImpl documentService;

    @TransactionalEventListener(
            classes = ClothDeleteEvent.class,
            phase = TransactionPhase.AFTER_COMMIT)
    public void handleClothDelete(ClothDeleteEvent event) {
        try {
            List<Long> historyIds = event.historyIds();
            if (historyIds.isEmpty()) {
                return;
            }

            List<HistoryDocument> documents = new ArrayList<>();
            for (Long historyId : historyIds) {
                try {
                    HistoryDocument document = documentService.toHistoryDocument(historyId);
                    documents.add(document);
                } catch (Exception e) {
                    log.warn("[search] HistoryDocument 변환 실패 - historyId: {}", historyId, e);
                }
            }

            if (!documents.isEmpty()) {
                searchRepository.saveAllHistories(documents);
                log.info(
                        "[search] Cloth 삭제로 인한 HistoryDocument 동기화 완료 - clothId: {}, historyCount: {}",
                        event.clothId(),
                        documents.size());
            }
        } catch (Exception e) {
            log.error("[search] Cloth 삭제 이벤트 처리 실패 - clothId: {}", event.clothId(), e);
        }
    }
}
