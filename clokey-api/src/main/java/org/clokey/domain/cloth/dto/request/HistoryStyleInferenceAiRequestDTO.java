package org.clokey.domain.cloth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HistoryStyleInferenceAiRequestDTO(
        @JsonProperty("download_url") String historyImageUrl) {}
