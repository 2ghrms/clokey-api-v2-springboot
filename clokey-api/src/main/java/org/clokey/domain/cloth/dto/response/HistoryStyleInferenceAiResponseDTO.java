package org.clokey.domain.cloth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record HistoryStyleInferenceAiResponseDTO(@JsonProperty("result") Result result) {

    public record Result(
            @JsonProperty("styles") List<StyleItem> styles,
            @JsonProperty("situations") List<SituationItem> situations) {}

    public record StyleItem(@JsonProperty("id") Long id, @JsonProperty("name") String name) {}

    public record SituationItem(@JsonProperty("id") Long id, @JsonProperty("name") String name) {}
}
