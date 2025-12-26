package org.clokey.exception;

import lombok.Getter;
import org.clokey.dto.ErrorReasonDto;

@Getter
public class BaseCustomException extends RuntimeException {

    private final BaseErrorCode code;

    public BaseCustomException(BaseErrorCode code) {
        super(code.getErrorReason().message());
        this.code = code;
    }

    public ErrorReasonDto getErrorReasonDto() {
        return this.code.getErrorReason();
    }
}
