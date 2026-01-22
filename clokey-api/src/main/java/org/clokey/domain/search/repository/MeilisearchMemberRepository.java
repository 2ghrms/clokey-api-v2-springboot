package org.clokey.domain.search.repository;

import io.vanslog.spring.data.meilisearch.repository.MeilisearchRepository;
import org.clokey.domain.search.document.MemberDocument;

public interface MeilisearchMemberRepository
        extends MeilisearchRepository<MemberDocument, String> {}
