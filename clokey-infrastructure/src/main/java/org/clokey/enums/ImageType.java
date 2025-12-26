package org.clokey.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ImageType {
    MEMBER_PROFILE("member-profile", "회원 프로필 사진"),
    MEMBER_BACKGROUND("member-background", "회원 배경 사진"),
    CLOTH_IMAGE("cloth-image", "옷 사진"),
    HISTORY_IMAGE("history-image", "기록 사진");

    private final String type;
    private final String description;
}
