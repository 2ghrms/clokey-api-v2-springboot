package org.clokey.domain.auth.service;

import org.clokey.domain.auth.dto.response.UserStatusResponse;

public interface AuthService {

    UserStatusResponse getUserStatus();
}
