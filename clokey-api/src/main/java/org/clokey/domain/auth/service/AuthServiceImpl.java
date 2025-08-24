package org.clokey.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.clokey.domain.auth.dto.AccessTokenDto;
import org.clokey.domain.auth.dto.RefreshTokenDto;
import org.clokey.domain.auth.dto.request.TokenReissueRequest;
import org.clokey.domain.auth.dto.response.TokenResponse;
import org.clokey.domain.auth.dto.response.UserStatusResponse;
import org.clokey.domain.auth.enums.RegisterStatus;
import org.clokey.domain.auth.exception.AuthErrorCode;
import org.clokey.domain.auth.repository.RefreshTokenRepository;
import org.clokey.domain.member.exception.MemberErrorCode;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.domain.term.repository.MemberTermRepository;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.util.MemberUtil;
import org.clokey.member.entity.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final MemberUtil memberUtil;

    private final MemberTermRepository memberTermRepository;
    private final MemberRepository memberRepository;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public UserStatusResponse getUserStatus() {
        final Member currentMember = memberUtil.getCurrentMember();

        if (memberTermRepository.existsByMemberId(currentMember.getId())) {
            return UserStatusResponse.of(RegisterStatus.REGISTERED);
        }
        return UserStatusResponse.of(RegisterStatus.NOT_AGREED);
    }

    @Override
    public TokenResponse reissueTokens(TokenReissueRequest request) {
        RefreshTokenDto oldRefreshToken =
                jwtTokenService.retrieveRefreshToken(request.refreshToken());

        if (oldRefreshToken == null) {
            throw new BaseCustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        RefreshTokenDto newRefreshTokenDto = jwtTokenService.reissueRefreshToken(oldRefreshToken);
        AccessTokenDto newAccessTokenDto =
                jwtTokenService.reissueAccessToken(getMember(newRefreshTokenDto));

        return TokenResponse.of(newAccessTokenDto.tokenValue(), newRefreshTokenDto.tokenValue());
    }

    @Override
    public void logoutUser() {
        final Member currentMember = memberUtil.getCurrentMember();

        refreshTokenRepository
                .findById(currentMember.getId())
                .ifPresent(refreshTokenRepository::delete);
    }

    private Member getMember(RefreshTokenDto refreshTokenDto) {
        return memberRepository
                .findById(refreshTokenDto.memberId())
                .orElseThrow(() -> new BaseCustomException(MemberErrorCode.MEMBER_NOT_FOUND));
    }
}
