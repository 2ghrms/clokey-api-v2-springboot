package org.clokey.domain.search.repository;

import io.vanslog.spring.data.meilisearch.repository.MeilisearchRepository;
import org.clokey.domain.search.document.HistoryDocument;

public interface MeilisearchHistoryRepository
        extends MeilisearchRepository<HistoryDocument, String> {}
