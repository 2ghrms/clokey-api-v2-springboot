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
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String clientId = userRequest.getClientRegistration().getClientId();
        String clientAuthenticationMethod =
                userRequest.getClientRegistration().getClientAuthenticationMethod().getValue();

        log.info(
                "[OIDC] 사용자 로드 시작 - Provider: {}, ClientId: {}, AuthMethod: {}",
                provider,
                clientId,
                clientAuthenticationMethod);

        try {
            log.info("[OIDC] Access Token 교환 시도 - Provider: {}", provider);
            OidcUser oidcUser = super.loadUser(userRequest);
            log.info("[OIDC] Access Token 교환 성공 - Provider: {}", provider);

            String oauthId = oidcUser.getName();
            String email = oidcUser.getAttribute("email");
            log.info(
                    "[OIDC] 사용자 정보 추출 - Provider: {}, OAuthId: {}, Email: {}",
                    provider,
                    oauthId,
                    email);

            OauthProvider oauthProvider = OauthProvider.valueOf(provider.toUpperCase());
            OauthInfo oauthInfo = OauthInfo.createOauthInfo(oauthId, oauthProvider);
            String resolvedEmail = resolveEmail(email, oauthId, oauthProvider);

            log.info("[OIDC] 회원 조회 시작 - Provider: {}, OAuthId: {}", provider, oauthId);
            Member member =
                    memberRepository
                            .findByOauthInfo(oauthInfo)
                            .orElseGet(
                                    () -> {
                                        log.info(
                                                "[OIDC] 신규 회원 생성 - Provider: {}, Email: {}",
                                                provider,
                                                email);
                                        Member newMember =
                                                Member.createMember(
                                                        resolvedEmail,
                                                        uniqueUtil.generateRandomNickname(),
                                                        oauthInfo);
                                        memberRepository.save(newMember);
                                        eventPublisher.publishEvent(
                                                MeiliSearchSyncEvent.of(
                                                        MeiliSearchSyncEvent.EntityType.MEMBER,
                                                        newMember.getId()));
                                        log.info(
                                                "[OIDC] 신규 회원 생성 완료 - MemberId: {}",
                                                newMember.getId());
                                        return newMember;
                                    });

            log.info("[OIDC] 사용자 로드 완료 - Provider: {}, MemberId: {}", provider, member.getId());
            return new CustomPrincipal(member, oidcUser.getAttributes(), oidcUser.getIdToken());
        } catch (OAuth2AuthenticationException e) {
            // OAuth2AuthenticationException은 그대로 전달 (이미 OAuth2Error 포함)
            log.error(
                    "[OIDC] 사용자 로드 실패 - Provider: {}, ErrorCode: {}, Description: {}",
                    provider,
                    e.getError() != null ? e.getError().getErrorCode() : "UNKNOWN",
                    e.getError() != null ? e.getError().getDescription() : e.getMessage(),
                    e);
            throw e;
        } catch (Exception e) {
            // 일반 Exception을 OAuth2AuthenticationException으로 변환
            log.error(
                    "[OIDC] 예상치 못한 오류 발생 - Provider: {}, Error: {}, Message: {}",
                    provider,
                    e.getClass().getSimpleName(),
                    e.getMessage(),
                    e);

            OAuth2Error oauth2Error =
                    new OAuth2Error(
                            OAuth2ErrorCodes.SERVER_ERROR,
                            "사용자 로드 중 오류 발생: " + e.getMessage(),
                            null);
            throw new OAuth2AuthenticationException(oauth2Error, e);
        }
    }

    private String resolveEmail(String email, String oauthId, OauthProvider oauthProvider) {
        if (StringUtils.hasText(email)) {
            return email;
        }

        String safeOauthId = oauthId.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (safeOauthId.length() > 80) {
            safeOauthId = safeOauthId.substring(0, 80);
        }
        String fallbackEmail =
                oauthProvider.name().toLowerCase() + "_" + safeOauthId + "@noemail.local";
        log.warn(
                "[OIDC] Email claim missing. Fallback email is used - Provider: {}, OAuthId: {}, FallbackEmail: {}",
                oauthProvider,
                oauthId,
                fallbackEmail);
        return fallbackEmail;
    }
}
