package org.clokey.domain.history.service;

import org.clokey.domain.history.dto.request.HistoryCreateRequest;
import org.clokey.domain.history.dto.request.HistoryUpdateRequest;
import org.clokey.domain.history.dto.response.*;

public interface HistoryService {
    HistoryCreateResponse createHistory(HistoryCreateRequest request);

    void updateHistory(Long historyId, HistoryUpdateRequest request);

    StyleListResponse getAllStyles();

    SituationListResponse getAllSituations();

    DailyHistoryResponse getDailyHistory(Long historyId);

    HistoryClothTagListResponse getHistoryClothTags(Long historyImageId);

    MonthlyHistoryResponse getMonthlyHistory(Long memberId, int year, int month);

    HistoryOwnershipCheckResponse checkHistoryOwnership(Long historyId);
}
