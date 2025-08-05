package org.clokey.member.entity;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.clokey.member.enums.SocialType;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OauthInfo {

    @NotNull private String oauthId;

    @Enumerated(EnumType.STRING)
    @NotNull
    private SocialType socialType;
}
