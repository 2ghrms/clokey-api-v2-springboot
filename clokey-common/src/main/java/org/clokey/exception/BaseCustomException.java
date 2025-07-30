package org.clokey.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.clokey.dto.ErrorReasonDto;

@Getter
@AllArgsConstructor
public class BaseCustomException extends RuntimeException {

    private BaseErrorCode code;

    public ErrorReasonDto getErrorReasonDto() {
        return this.code.getErrorReason();
    }
}
