package org.clokey.domain.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.clokey.code.GlobalBaseSuccessCode;
import org.clokey.domain.auth.dto.response.TokenResponse;
import org.clokey.domain.auth.service.JwtTokenService;
import org.clokey.global.security.CustomPrincipal;
import org.clokey.member.entity.Member;
import org.clokey.response.BaseResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
@Slf4j
public class OidcLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenService jwtTokenService;
    private final ObjectMapper objectMapper;

    @Value("${app.oauth.redirect-scheme:clokey}")
    private String redirectScheme;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {

        CustomPrincipal principal = (CustomPrincipal) authentication.getPrincipal();
        Member member = principal.getMember();

        String accessToken =
                jwtTokenService.createAccessToken(member.getId(), member.getMemberRole());
        String refreshToken = jwtTokenService.createRefreshToken(member.getId());

        TokenResponse tokenResponse = TokenResponse.of(accessToken, refreshToken);
        BaseResponse<TokenResponse> jsonResponse =
                BaseResponse.onSuccess(GlobalBaseSuccessCode.CREATED, tokenResponse);
        String jsonData = objectMapper.writeValueAsString(jsonResponse);

        String redirectUrl =
                UriComponentsBuilder.fromUriString(redirectScheme + "://oauth/callback")
                        .queryParam("accessToken", accessToken)
                        .queryParam("refreshToken", refreshToken)
                        .build()
                        .toUriString();

        String html = buildHtmlPage(jsonData, redirectUrl);

        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(html);
        response.getWriter().flush();
    }

    // NOTE : 개발자들을 위해서 웹에서 보여주면서도 딥링크 리다이랙트를 동시에 보내줌.
    private String buildHtmlPage(String jsonData, String redirectUrl) {
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<meta charset='UTF-8'>"
                + "<title>OAuth Login Success</title>"
                + "</head>"
                + "<body>"
                + "<pre>"
                + jsonData
                + "</pre>"
                + "<script>"
                + "window.location.href = '"
                + redirectUrl
                + "';"
                + "</script>"
                + "</body>"
                + "</html>";
    }
}
