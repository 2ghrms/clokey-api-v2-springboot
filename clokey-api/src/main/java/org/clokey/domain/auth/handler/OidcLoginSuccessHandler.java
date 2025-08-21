package org.clokey.domain.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.clokey.code.GlobalBaseSuccessCode;
import org.clokey.domain.auth.dto.response.TokenResponse;
import org.clokey.domain.auth.service.JwtTokenService;
import org.clokey.global.security.CustomPrincipal;
import org.clokey.member.entity.Member;
import org.clokey.response.BaseResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OidcLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenService jwtTokenService;
    private final ObjectMapper objectMapper;

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

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(
                response.getWriter(),
                BaseResponse.onSuccess(GlobalBaseSuccessCode.CREATED, tokenResponse));
    }
}
