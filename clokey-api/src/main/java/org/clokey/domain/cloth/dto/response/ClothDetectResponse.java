package org.clokey.domain.cloth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "사진에서 옷 탐지 응답")
public record ClothDetectResponse(@Schema(description = "옷 정보 목록") List<Payload> payloads) {

    public static ClothDetectResponse of(List<Payload> payloads) {
        return new ClothDetectResponse(payloads);
    }

    @Schema(name = "ClothDetectResponsePayload", description = "옷 정보")
    public record Payload(
            @Schema(description = "새로운 옷 사진 URL", example = "https://example.com/cloth.jpg")
                    String clothImageUrl) {}
}
