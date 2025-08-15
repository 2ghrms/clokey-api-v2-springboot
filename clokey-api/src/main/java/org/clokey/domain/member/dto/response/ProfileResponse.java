package org.clokey.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.clokey.member.entity.Member;
import org.clokey.member.enums.Visibility;

// 추후 프로필조회를 위해 유지.
public record ProfileResponse(
        @Schema(description = "회원 ID", example = "1") Long id,
        @Schema(description = "한 줄 소개", example = "한줄소개") String bio,
        @Schema(description = "이메일", example = "juwon@gmail.com") String email,
        @Schema(description = "닉네임", example = "팽이") String nickname,
        @Schema(description = "Clokey ID", example = "juwon") String clokeyId,
        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile/john.jpg")
                String profileImageUrl,
        @Schema(description = "배경 이미지 URL", example = "https://example.com/profile/john.jpg")
                String profileBackImageUrl,
        @Schema(description = "계정 공개 여부", example = "PUBLIC") Visibility visibility) {
    public static ProfileResponse from(Member member) {
        return new ProfileResponse(
                member.getId(),
                member.getBio(),
                member.getEmail(),
                member.getNickname(),
                member.getClokeyId(),
                member.getProfileImageUrl(),
                member.getProfileBackImageUrl(),
                member.getVisibility());
    }
}
