package org.clokey.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.clokey.member.enums.Visibility;

public record ProfileUpdateRequest(
        @NotBlank(message = "닉네임은 비워둘 수 없습니다.") @Schema(description = "사용자의 닉네임", example = "팽이")
                String nickname,
        @NotBlank(message = "Clokey ID는 비워둘 수 없습니다.")
                @Schema(description = "사용자의 Clokey ID", example = "juwon")
                String clokeyId,
        @Schema(description = "사용자의 한줄 소개", example = "한줄 소개")
                @Size(max = 100, message = "바이오는 100자를 넘길 수 없습니다.")
                String bio,
        @NotNull(message = "공개여부는 비워둘 수 없습니다.") @Schema(description = "계정 공개여부", example = "PUBLIC")
                Visibility visibility,
        @Schema(description = "사용자 프로필 이미지", example = "profile.jpg") String profileImageUrl,
        @Schema(description = "사용자 배경 이미지", example = "background.jpg")
                String profileBackImageUrl) {}
