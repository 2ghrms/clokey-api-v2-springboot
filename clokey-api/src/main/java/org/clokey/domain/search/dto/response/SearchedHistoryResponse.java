package org.clokey.domain.search.dto.response;

public record SearchedHistoryResponse(
        Long historyId, String historyImageUrl, String profileImageUrl, String nickname) {}
