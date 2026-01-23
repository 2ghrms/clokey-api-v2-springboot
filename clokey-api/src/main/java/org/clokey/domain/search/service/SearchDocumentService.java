package org.clokey.domain.search.service;

import org.clokey.domain.search.document.HistoryDocument;
import org.clokey.domain.search.document.MemberDocument;

public interface SearchDocumentService {

    HistoryDocument toHistoryDocument(Long historyId);

    MemberDocument toMemberDocument(Long memberId);
}
