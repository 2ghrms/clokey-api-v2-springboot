package org.clokey.domain.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.clokey.exception.GlobalBaseErrorCode;
import org.clokey.response.BaseResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OidcLoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException {

        String requestURI = request.getRequestURI();
        String queryString = request.getQueryString();
        String errorCode = null;
        String errorDescription = null;

        log.error(
                "[OIDC Failure] 로그인 실패 핸들러 시작 - RequestURI: {}, QueryString: {}",
                requestURI,
                queryString);
        log.error(
                "[OIDC Failure] 예외 정보 - ExceptionType: {}, Message: {}",
                exception.getClass().getSimpleName(),
                exception.getMessage());

        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            errorCode = oauth2Exception.getError().getErrorCode();
            errorDescription = oauth2Exception.getError().getDescription();
            log.error(
                    "[OIDC Failure] OAuth2 예외 상세 - ErrorCode: {}, Description: {}",
                    errorCode,
                    errorDescription);

            if (oauth2Exception.getCause() != null) {
                log.error(
                        "[OIDC Failure] 원인 예외 - CauseType: {}, CauseMessage: {}",
                        oauth2Exception.getCause().getClass().getSimpleName(),
                        oauth2Exception.getCause().getMessage(),
                        oauth2Exception.getCause());
            }
        }

        // 요청 파라미터 로깅
        request.getParameterMap()
                .forEach(
                        (key, values) ->
                                log.error(
                                        "[OIDC Failure] Request Parameter - {}: {}",
                                        key,
                                        String.join(", ", values)));

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String errorMessage = errorDescription != null ? errorDescription : exception.getMessage();
        BaseResponse<Void> failureResponse =
                BaseResponse.onFailure(
                        GlobalBaseErrorCode.UNAUTHORIZED.getCode(), errorMessage, null);

        String json = objectMapper.writeValueAsString(failureResponse);
        response.getWriter().write(json);

        log.error("[OIDC Failure] 실패 응답 전송 완료 - ErrorCode: {}", errorCode);
    }
}
