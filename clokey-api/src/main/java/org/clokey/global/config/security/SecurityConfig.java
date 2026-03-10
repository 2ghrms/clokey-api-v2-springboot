package org.clokey.global.config.security;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.clokey.domain.auth.handler.OidcLoginSuccessHandler;
import org.clokey.domain.auth.service.CustomOAuth2UserService;
import org.clokey.domain.auth.service.JwtTokenService;
import org.clokey.global.security.AppleAwareOAuth2AuthorizationRequestResolver;
import org.clokey.global.security.JwtAuthenticationFilter;
import org.clokey.global.security.SwaggerBasicAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] SWAGGER_PATHS = {
        "/swagger-ui", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs", "/v3/api-docs/**"
    };

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OidcLoginSuccessHandler oidcLoginSuccessHandler;

    private void defaultFilterChain(HttpSecurity http) throws Exception {
        http.httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        session ->
                                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain apiFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            OAuth2AuthorizationRequestResolver authorizationRequestResolver,
            SwaggerBasicAuthenticationFilter swaggerBasicAuthenticationFilter)
            throws Exception {
        defaultFilterChain(http);

        http.authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers("/public/**")
                                        .permitAll()
                                        .requestMatchers("/auth/reissue-token")
                                        .permitAll()
                                        .requestMatchers("/oauth2/**", "/login/oauth2/**")
                                        .permitAll()
                                        .requestMatchers(SWAGGER_PATHS)
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .oauth2Login(
                        oauth2 -> {
                            oauth2.userInfoEndpoint(
                                            userInfo ->
                                                    userInfo.oidcUserService(
                                                            customOAuth2UserService))
                                    .successHandler(oidcLoginSuccessHandler);
                            oauth2.authorizationEndpoint(
                                    a ->
                                            a.authorizationRequestResolver(
                                                    authorizationRequestResolver));
                        })
                .addFilterBefore(
                        swaggerBasicAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(
                        jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(
                List.of(
                        "http://localhost:3000",
                        "https://dev.clokey.store",
                        "https://prod.clokey.store"));
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        CorsConfiguration appleCallbackConfiguration = new CorsConfiguration();
        appleCallbackConfiguration.setAllowedOriginPatterns(List.of("https://appleid.apple.com"));
        appleCallbackConfiguration.setAllowedMethods(List.of("POST", "OPTIONS"));
        appleCallbackConfiguration.setAllowedHeaders(List.of("*"));
        appleCallbackConfiguration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/login/oauth2/code/**", appleCallbackConfiguration);
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        return new JwtAuthenticationFilter(jwtTokenService);
    }

    @Bean
    public OAuth2AuthorizationRequestResolver oauth2AuthorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {
        AppleAwareOAuth2AuthorizationRequestResolver resolver =
                new AppleAwareOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository, "/oauth2/authorization");

        resolver.setAuthorizationRequestCustomizer(
                OAuth2AuthorizationRequestCustomizers.withPkce());
        return resolver;
    }
}
