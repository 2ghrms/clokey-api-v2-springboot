package org.clokey.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record DuplicatedIdCheckResponse(
        @Schema(description = "중복 여부", example = "true") boolean duplicated) {
    public static DuplicatedIdCheckResponse of(boolean duplicated) {
        return new DuplicatedIdCheckResponse(duplicated);
    }
}
