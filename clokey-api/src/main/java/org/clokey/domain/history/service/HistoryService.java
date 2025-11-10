package org.clokey.domain.history.service;

import org.clokey.domain.history.dto.request.HistoryCreateRequest;
import org.clokey.domain.history.dto.response.HistoryCreateResponse;

public interface HistoryService {
    HistoryCreateResponse createHistory(HistoryCreateRequest request);
}
