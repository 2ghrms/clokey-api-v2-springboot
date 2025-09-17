package org.clokey.domain.cloth.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.clokey.dto.ErrorReasonDto;
import org.clokey.exception.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum ClothErrorCode implements BaseErrorCode {
    DUPLICATED_CLOTH(400, "CLOTH_4001", "중복된 옷이 존재합니다."),

    NOT_CLOTH_OWNER(403, "CLOTH_4031", "나의 옷이 아닙니다. 권한이 없습니다."),

    ClOTH_NOT_FOUND(404, "CLOTH_4041", "존재하지 않는 옷 입니다.");

    private final int status;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDto getErrorReason() {
        return ErrorReasonDto.of(status, code, message);
    }
}
