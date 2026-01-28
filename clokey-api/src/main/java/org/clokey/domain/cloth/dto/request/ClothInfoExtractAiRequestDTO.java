package org.clokey.domain.cloth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ClothInfoExtractAiRequestDTO(
        @JsonProperty("download_urls") List<String> clothImageUrls,
        @JsonProperty("upload_urls") List<String> presignedUrls) {}
