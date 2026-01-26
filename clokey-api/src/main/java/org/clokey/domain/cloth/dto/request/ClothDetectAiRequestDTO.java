package org.clokey.domain.cloth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ClothDetectAiRequestDTO(
        @JsonProperty("download_url") String imageUrl,
        @JsonProperty("upload_urls") List<String> presignedUrls) {}
