package org.clokey.domain.auth.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.clokey.auth.entity.RefreshToken;
import org.clokey.domain.auth.dto.AccessTokenDto;
import org.clokey.domain.auth.dto.RefreshTokenDto;
import org.clokey.domain.auth.exception.AuthErrorCode;
import org.clokey.domain.auth.repository.RefreshTokenRepository;
import org.clokey.domain.auth.util.JwtUtil;
import org.clokey.exception.BaseCustomException;
import org.clokey.member.entity.Member;
import org.clokey.member.enums.MemberRole;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public String createAccessToken(Long memberId, MemberRole memberRole) {
        return jwtUtil.generateAccessToken(memberId, memberRole);
    }

    public String createRefreshToken(Long memberId) {
        String token = jwtUtil.generateRefreshToken(memberId);
        RefreshToken refreshToken =
                RefreshToken.builder()
                        .memberId(memberId)
                        .token(token)
                        .ttl(jwtUtil.getRefreshTokenExpirationTime())
                        .build();
        refreshTokenRepository.save(refreshToken);

        return token;
    }

    public AccessTokenDto reissueAccessToken(Member member) {
        return jwtUtil.generateAccessTokenDto(member.getId(), member.getMemberRole());
    }

    public RefreshTokenDto reissueRefreshToken(RefreshTokenDto oldRefreshTokenDto) {
        RefreshToken refreshToken =
                refreshTokenRepository
                        .findById(oldRefreshTokenDto.memberId())
                        .orElseThrow(
                                () ->
                                        new BaseCustomException(
                                                AuthErrorCode.REFRESH_TOKEN_NOT_FOUND));

        RefreshTokenDto newRefreshTokenDto =
                jwtUtil.generateRefreshTokenDto(refreshToken.getMemberId());
        refreshToken.updateRefreshToken(newRefreshTokenDto.tokenValue(), newRefreshTokenDto.ttl());

        refreshTokenRepository.save(refreshToken);

        return newRefreshTokenDto;
    }

    public RefreshTokenDto retrieveRefreshToken(String refreshTokenValue) {
        RefreshTokenDto refreshTokenDto;
        try {
            refreshTokenDto = jwtUtil.parseRefreshToken(refreshTokenValue);
        } catch (Exception e) {
            return null;
        }

        Optional<RefreshToken> refreshToken =
                refreshTokenRepository.findById(refreshTokenDto.memberId());

        if (refreshToken.isEmpty()) {
            return null;
        }

        if (!refreshTokenDto.tokenValue().equals(refreshToken.get().getToken())) {
            return null;
        }

        return refreshTokenDto;
    }

    public AccessTokenDto retrieveAccessToken(String accessTokenValue) {
        try {
            return jwtUtil.parseAccessToken(accessTokenValue);
        } catch (Exception e) {
            return null;
        }
    }
}
