package org.clokey.domain.term.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record TermAgreeRequest(
        @NotEmpty(message = "약관 동의 정보는 비워둘 수 없습니다.") @Valid @Schema(description = "약관 동의 정보 리스트")
                List<Payload> payloads) {
    public record Payload(
            @NotNull(message = "약관 ID는 비워둘 수 없습니다.") @Schema(description = "약관 ID") Long termId,
            @NotNull(message = "약관 동의 여부는 비워둘 수 없습니다.")
                    @Schema(description = "약관 동의 여부", example = "true")
                    Boolean agreed) {}
}
