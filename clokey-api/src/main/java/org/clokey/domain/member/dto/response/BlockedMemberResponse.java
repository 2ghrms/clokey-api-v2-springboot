package org.clokey.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.clokey.member.entity.Block;
import org.clokey.member.entity.Member;

public record BlockedMemberResponse(
        @Schema(description = "Block(차단)의 ID", example = "1") Long blockId,
        @Schema(description = "차단된 멤버의 ID", example = "1") Long memberId,
        @Schema(description = "차단된 멤버의 닉네임", example = "clokey123") String nickname,
        @Schema(
                        description = "차단됨 멤버의 프로필 이미지 URL",
                        example = "https://example.com/profile/john.jpg")
                String profileImageUrl) {
    public static BlockedMemberResponse from(Block block) {
        Member blockedMember = block.getBlocked();

        return new BlockedMemberResponse(
                block.getId(),
                blockedMember.getId(),
                blockedMember.getNickname(),
                blockedMember.getProfileImageUrl());
    }
}
