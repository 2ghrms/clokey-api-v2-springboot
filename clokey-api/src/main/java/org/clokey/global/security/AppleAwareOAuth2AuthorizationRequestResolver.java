package org.clokey.global.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
public class AppleAwareOAuth2AuthorizationRequestResolver
        implements OAuth2AuthorizationRequestResolver {

    private final DefaultOAuth2AuthorizationRequestResolver delegate;

    public AppleAwareOAuth2AuthorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository,
            String authorizationBaseUri) {
        this.delegate =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository, authorizationBaseUri);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = delegate.resolve(request);
        return customizeIfApple(request, authorizationRequest);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(
            HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest =
                delegate.resolve(request, clientRegistrationId);
        if (authorizationRequest == null) {
            return null;
        }
        return "apple".equals(clientRegistrationId)
                ? enforceAppleResponseMode(authorizationRequest)
                : authorizationRequest;
    }

    public void setAuthorizationRequestCustomizer(
            java.util.function.Consumer<OAuth2AuthorizationRequest.Builder>
                    authorizationRequestCustomizer) {
        delegate.setAuthorizationRequestCustomizer(authorizationRequestCustomizer);
    }

    private OAuth2AuthorizationRequest customizeIfApple(
            HttpServletRequest request, OAuth2AuthorizationRequest authorizationRequest) {
        if (authorizationRequest == null) {
            return null;
        }
        String requestUri = request.getRequestURI();
        if (requestUri != null && requestUri.endsWith("/apple")) {
            return enforceAppleResponseMode(authorizationRequest);
        }
        return authorizationRequest;
    }

    private OAuth2AuthorizationRequest enforceAppleResponseMode(
            OAuth2AuthorizationRequest authorizationRequest) {
        String authorizationRequestUri =
                UriComponentsBuilder.fromUriString(
                                authorizationRequest.getAuthorizationRequestUri())
                        .replaceQueryParam("response_mode", "form_post")
                        .build(true)
                        .toUriString();

        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .additionalParameters(params -> params.put("response_mode", "form_post"))
                .authorizationRequestUri(authorizationRequestUri)
                .build();
    }
}
