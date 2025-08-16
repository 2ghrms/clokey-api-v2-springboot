package org.clokey.domain.auth.dto.response;

import org.clokey.domain.auth.enums.RegisterStatus;

public record UserStatusResponse(RegisterStatus registerStatus) {
    public static UserStatusResponse of(RegisterStatus registerStatus) {
        return new UserStatusResponse(registerStatus);
    }
}
