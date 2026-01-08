package org.clokey.global.config.security;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.clokey.domain.auth.handler.OidcLoginSuccessHandler;
import org.clokey.domain.auth.service.CustomOAuth2UserService;
import org.clokey.domain.auth.service.JwtTokenService;
import org.clokey.global.security.JwtAuthenticationFilter;
import org.clokey.helper.SpringEnvironmentHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final SpringEnvironmentHelper springEnvironmentHelper;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OidcLoginSuccessHandler oidcLoginSuccessHandler;
    private final ApplicationContext applicationContext;

    @Autowired(required = false)
    private ClientRegistrationRepository clientRegistrationRepository;

    @Value("${swagger.username:default}")
    private String swaggerUsername;

    @Value("${swagger.password:default}")
    private String swaggerPassword;

    private void defaultFilterChain(HttpSecurity http) throws Exception {
        http.httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    }

    @Bean
    public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
        UserDetails user =
                User.withUsername(swaggerUsername)
                        .password(passwordEncoder().encode(swaggerPassword))
                        .roles("SWAGGER")
                        .build();

        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(1)
    @Profile({"dev", "local", "prod"})
    public SecurityFilterChain swaggerFilterChain(HttpSecurity http) throws Exception {
        defaultFilterChain(http);

        http.securityMatcher("/swagger-ui/**", "/v3/api-docs/**").httpBasic(withDefaults());

        http.authorizeHttpRequests(
                (springEnvironmentHelper.isDevProfile())
                        ? authorize -> authorize.anyRequest().authenticated()
                        : authorize -> authorize.anyRequest().permitAll());

        return http.build();
    }

    /** 인증 없이 제공하고 싶은 API는 /public 으로 시작해야 합니다. */
    @Bean
    @Order(2)
    @Profile({"local", "dev", "prod"})
    public SecurityFilterChain apiFilterChain(
            HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        defaultFilterChain(http);

        http.authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(
                                                "/public/**", "/swagger-ui/**", "/v3/api-docs/**")
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
                            if (clientRegistrationRepository != null) {
                                try {
                                    OAuth2AuthorizationRequestResolver resolver =
                                            applicationContext.getBean(
                                                    OAuth2AuthorizationRequestResolver.class);
                                    oauth2.authorizationEndpoint(
                                            authorization ->
                                                    authorization.authorizationRequestResolver(
                                                            resolver));
                                } catch (Exception e) {
                                    // Resolver bean이 없으면 기본 resolver 사용
                                }
                            }
                        })
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

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        return new JwtAuthenticationFilter(jwtTokenService);
    }

    @Bean
    @ConditionalOnBean(ClientRegistrationRepository.class)
    public OAuth2AuthorizationRequestResolver oauth2AuthorizationRequestResolver() {
        if (clientRegistrationRepository == null) {
            throw new IllegalStateException("ClientRegistrationRepository is required for OAuth2");
        }
        DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository, "/oauth2/authorization");
        resolver.setAuthorizationRequestCustomizer(
                OAuth2AuthorizationRequestCustomizers.withPkce());
        return resolver;
    }
}
