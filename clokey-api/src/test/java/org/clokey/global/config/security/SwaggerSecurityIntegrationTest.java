package org.clokey.global.config.security;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "local"})
@TestPropertySource(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false;MODE=MYSQL",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.datasource.username=sa",
            "spring.datasource.password=",
            "spring.flyway.enabled=false",
            "spring.jpa.hibernate.ddl-auto=none",
            "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
            "jwt.access-token-secret=test-access-secret-test-access-secret",
            "jwt.refresh-token-secret=test-refresh-secret-test-refresh-secret",
            "jwt.access-token-expiration-time=3600000",
            "jwt.refresh-token-expiration-time=1209600000",
            "jwt.issuer=test-issuer",
            "spring.security.oauth2.client.registration.kakao.client-id=test",
            "spring.security.oauth2.client.registration.kakao.client-secret=test",
            "spring.security.oauth2.client.registration.kakao.redirect-uri=http://localhost/login/oauth2/code/kakao",
            "spring.security.oauth2.client.registration.apple.client-id=test",
            "spring.security.oauth2.client.registration.apple.client-secret=test",
            "spring.security.oauth2.client.registration.apple.redirect-uri=http://localhost/login/oauth2/code/apple",
            "external.api.ai-server-ip=http://localhost",
            "external.api.cloth-inference-path=/cloth",
            "external.api.style-inference-path=/style",
            "external.api.cloth-detect-path=/detect",
            "firebase.credentials-path=dummy"
        })
class SwaggerSecurityIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private FirebaseMessaging mockFirebaseMessaging;

    @Test
    void swaggerUiIndexWithoutCredentialsDoesNotRedirectToLogin() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", containsString("Basic")));
    }

    @Test
    void swaggerConfigWithoutCredentialsDoesNotRedirectToLogin() throws Exception {
        mockMvc.perform(get("/v3/api-docs/swagger-config"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", containsString("Basic")));
    }

    @Test
    void reissueTokenWithoutAuthenticationReturnsJsonValidationError() throws Exception {
        mockMvc.perform(
                        post("/auth/reissue-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"refreshToken\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400"))
                .andExpect(jsonPath("$.result.refreshToken").exists());
    }
}
