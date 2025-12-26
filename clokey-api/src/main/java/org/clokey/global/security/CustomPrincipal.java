package org.clokey.global.security;

import java.util.Collection;
import java.util.Map;
import lombok.Getter;
import org.clokey.member.entity.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

@Getter
public class CustomPrincipal implements OidcUser {

    private final Member member;
    private final Map<String, Object> attributes;
    private final OidcIdToken idToken;

    public CustomPrincipal(Member member, Map<String, Object> attributes, OidcIdToken idToken) {
        this.member = member;
        this.attributes = attributes;
        this.idToken = idToken;
    }

    @Override
    public Map<String, Object> getClaims() {
        return attributes;
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return null;
    }

    @Override
    public OidcIdToken getIdToken() {
        return idToken;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return java.util.List.of(() -> "ROLE_USER");
    }

    @Override
    public String getName() {
        return member.getId().toString();
    }
}
