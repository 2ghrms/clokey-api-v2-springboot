package org.clokey.domain.cloth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ClothDetectAiResponseDTO(
        @JsonProperty("isSuccess") Boolean isSuccess,
        String message,
        Result result,
        @JsonProperty("error_code") String errorCode) {

    public record Result(
            @JsonProperty("detected_cnt") Integer detectedCnt,
            @JsonProperty("uploaded_cnt") Integer uploadedCnt,
            @JsonProperty("uploaded_urls") List<String> uploadedUrls) {}
}
