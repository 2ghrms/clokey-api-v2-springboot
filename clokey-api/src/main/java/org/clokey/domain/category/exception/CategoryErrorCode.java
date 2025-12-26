package org.clokey.domain.category.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.clokey.dto.ErrorReasonDto;
import org.clokey.exception.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum CategoryErrorCode implements BaseErrorCode {
    CATEGORY_NOT_FOUND(404, "CATEGORY_4041", "존재하지 않는 카테고리입니다."),
    CATEGORY_IN_BULK_NOT_FOUND(404, "CATEGORY_4042", "존재하지 않는 카테고리가 포함되어 있습니다.");

    private final int status;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDto getErrorReason() {
        return ErrorReasonDto.of(status, code, message);
    }
}
