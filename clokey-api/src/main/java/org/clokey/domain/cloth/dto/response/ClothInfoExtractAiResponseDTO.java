package org.clokey.domain.cloth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ClothInfoExtractAiResponseDTO(
        @JsonProperty("isSuccess") Boolean isSuccess,
        String message,
        List<ResultItem> result,
        @JsonProperty("error_code") String errorCode) {

    public record ResultItem(
            @JsonProperty("categories") List<CategoryItem> categories,
            @JsonProperty("seasons") List<SeasonItem> seasons,
            @JsonProperty("uploaded_url") String uploadedUrl) {}

    public record CategoryItem(@JsonProperty("id") Long id, @JsonProperty("name") String name) {}

    public record SeasonItem(@JsonProperty("id") Long id, @JsonProperty("name") String name) {}
}
