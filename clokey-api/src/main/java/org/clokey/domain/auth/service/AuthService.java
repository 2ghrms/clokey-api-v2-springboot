package org.clokey.domain.auth.service;

import org.clokey.domain.auth.dto.request.DeviceTokenRenewRequest;
import org.clokey.domain.auth.dto.request.TokenReissueRequest;
import org.clokey.domain.auth.dto.response.TokenResponse;
import org.clokey.domain.auth.dto.response.UserStatusResponse;

public interface AuthService {

    UserStatusResponse getUserStatus();

    void renewDeviceToken(DeviceTokenRenewRequest request);

    TokenResponse reissueTokens(TokenReissueRequest request);

    void logoutUser();
}
