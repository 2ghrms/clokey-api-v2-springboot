package org.clokey.domain.auth.service;

import org.clokey.domain.auth.dto.request.TokenReissueRequest;
import org.clokey.domain.auth.dto.response.TokenResponse;
import org.clokey.domain.auth.dto.response.UserStatusResponse;

public interface AuthService {

    UserStatusResponse getUserStatus();

    TokenResponse reissueTokens(TokenReissueRequest request);

    void logoutUser();
}
