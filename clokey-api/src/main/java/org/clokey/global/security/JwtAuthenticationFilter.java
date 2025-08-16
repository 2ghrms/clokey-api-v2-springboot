package org.clokey.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.clokey.domain.auth.dto.AccessTokenDto;
import org.clokey.domain.auth.util.JwtUtil;
import org.clokey.member.enums.MemberRole;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String accessTokenValue = resolveToken(request);

        if (accessTokenValue != null) {
            AccessTokenDto accessTokenDto = jwtUtil.parseAccessToken(accessTokenValue);

            // 유효한 Access Token
            if (accessTokenDto != null) {
                setAuthenticationToken(accessTokenDto.memberId(), accessTokenDto.role());
            }
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthenticationToken(Long memberId, MemberRole memberRole) {
        UserDetails userDetails =
                User.withUsername(memberId.toString())
                        .password("")
                        .authorities(memberRole.toString())
                        .build();

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(token);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
