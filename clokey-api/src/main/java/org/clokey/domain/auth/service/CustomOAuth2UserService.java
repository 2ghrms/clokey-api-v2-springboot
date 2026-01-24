package org.clokey.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.clokey.domain.auth.util.UniqueUtil;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.domain.search.event.MeiliSearchSyncEvent;
import org.clokey.global.security.CustomPrincipal;
import org.clokey.member.entity.Member;
import org.clokey.member.entity.OauthInfo;
import org.clokey.member.enums.OauthProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends OidcUserService {

    private final MemberRepository memberRepository;
    private final UniqueUtil uniqueUtil;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        String oauthId = oidcUser.getName();
        String email = oidcUser.getAttribute("email");

        OauthProvider oauthProvider = OauthProvider.valueOf(provider.toUpperCase());
        OauthInfo oauthInfo = OauthInfo.createOauthInfo(oauthId, oauthProvider);

        Member member =
                memberRepository
                        .findByOauthInfo(oauthInfo)
                        .orElseGet(
                                () -> {
                                    Member newMember =
                                            Member.createMember(
                                                    email,
                                                    uniqueUtil.generateRandomNickname(),
                                                    oauthInfo);
                                    memberRepository.save(newMember);
                                    eventPublisher.publishEvent(
                                            MeiliSearchSyncEvent.of(
                                                    MeiliSearchSyncEvent.EntityType.MEMBER,
                                                    newMember.getId()));
                                    return newMember;
                                });

        return new CustomPrincipal(member, oidcUser.getAttributes(), oidcUser.getIdToken());
    }
}
