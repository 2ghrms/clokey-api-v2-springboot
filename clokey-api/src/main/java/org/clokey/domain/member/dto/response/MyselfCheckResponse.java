package org.clokey.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MyselfCheckResponse(
        @Schema(description = "본인 여부", example = "true") boolean isMyself) {
    public static MyselfCheckResponse of(boolean isMyself) {
        return new MyselfCheckResponse(isMyself);
    }
}
