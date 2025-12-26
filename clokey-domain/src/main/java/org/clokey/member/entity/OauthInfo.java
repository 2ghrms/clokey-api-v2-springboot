package org.clokey.member.entity;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.clokey.member.enums.OauthProvider;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OauthInfo {

    @NotNull private String oauthId;

    @Enumerated(EnumType.STRING)
    @NotNull
    private OauthProvider oauthProvider;

    @Builder(access = AccessLevel.PRIVATE)
    private OauthInfo(String oauthId, OauthProvider oauthProvider) {
        this.oauthId = oauthId;
        this.oauthProvider = oauthProvider;
    }

    public static OauthInfo createOauthInfo(String oauthId, OauthProvider oauthProvider) {
        return OauthInfo.builder().oauthId(oauthId).oauthProvider(oauthProvider).build();
    }
}
