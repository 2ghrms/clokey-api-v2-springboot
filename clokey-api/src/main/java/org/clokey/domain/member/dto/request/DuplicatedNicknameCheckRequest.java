package org.clokey.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DuplicatedNicknameCheckRequest(
        @NotBlank(message = "닉네임은 비워둘 수 없습니다.")
                @Size(max = 20, message = "닉네임은 20자 이하여야 합니다.")
                @Pattern(
                        regexp = "^[a-z0-9가-힣._]+$",
                        message = "닉네임은 영어 소문자, 숫자, 한글, 언더바(_), 점(.)만 허용됩니다.")
                @Schema(description = "중복을 확인할 닉네임", example = "clokey.홍길동")
                String nickname) {}
