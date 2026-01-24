package org.clokey.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record DuplicatedIdCheckRequest(
        @NotNull(message = "닉네임은 비워둘 수 없습니다.")
                @Pattern(
                        regexp = "^[a-z0-9._]+$",
                        message = "닉네임은 영어 소문자, 숫자, 언더바(_), 점(.)만 허용됩니다.")
                @Schema(description = "중복을 확인할 닉네임", example = "clokey11")
                String nickname) {}
