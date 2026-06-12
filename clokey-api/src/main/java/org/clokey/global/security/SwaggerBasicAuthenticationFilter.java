package org.clokey.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class SwaggerBasicAuthenticationFilter extends OncePerRequestFilter {

    private static final String[] SWAGGER_PATHS = {
        "/swagger-ui", "/swagger-ui/", "/swagger-ui.html", "/v3/api-docs"
    };

    @Value("${swagger.username:default}")
    private String swaggerUsername;

    @Value("${swagger.password:default}")
    private String swaggerPassword;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();

        for (String swaggerPath : SWAGGER_PATHS) {
            if (uri.equals(swaggerPath) || uri.startsWith(swaggerPath + "/")) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Basic ")) {
            writeUnauthorized(response);
            return;
        }

        String[] credentials = decodeCredentials(authorization.substring(6));
        if (credentials == null) {
            writeUnauthorized(response);
            return;
        }

        if (!swaggerUsername.equals(credentials[0]) || !swaggerPassword.equals(credentials[1])) {
            writeUnauthorized(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String[] decodeCredentials(String encodedCredentials) {
        try {
            String decoded =
                    new String(
                            Base64.getDecoder().decode(encodedCredentials), StandardCharsets.UTF_8);
            int separatorIndex = decoded.indexOf(':');
            if (separatorIndex < 0) {
                return null;
            }

            return new String[] {
                decoded.substring(0, separatorIndex), decoded.substring(separatorIndex + 1)
            };
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader("WWW-Authenticate", "Basic realm=\"Swagger\"");
        response.getWriter().flush();
    }
}
