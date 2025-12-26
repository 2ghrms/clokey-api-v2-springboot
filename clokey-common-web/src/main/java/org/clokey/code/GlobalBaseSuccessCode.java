package org.clokey.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.clokey.dto.SuccessReasonDto;

@Getter
@AllArgsConstructor
public enum GlobalBaseSuccessCode implements BaseSuccessCode {
    OK(200, "COMMON200", "성공입니다."),
    CREATED(201, "COMMON201", "요청 성공 및 리소스 생성됨"),
    NO_CONTENT(204, "COMMON204", "요청 성공 및 반환값 없음");

    private final int status;
    private final String code;
    private final String message;

    @Override
    public SuccessReasonDto getReasonDto() {
        return SuccessReasonDto.of(status, code, message);
    }
}
