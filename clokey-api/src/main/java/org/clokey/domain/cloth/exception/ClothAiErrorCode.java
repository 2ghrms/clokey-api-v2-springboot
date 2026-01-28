package org.clokey.domain.cloth.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.clokey.dto.ErrorReasonDto;
import org.clokey.exception.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum ClothAiErrorCode implements BaseErrorCode {
    AI_SERVER_REQUEST_FAILED(502, "CLOTH_AI_5021", "AI 서버 요청에 실패했습니다."),
    AI_SERVER_INVALID_RESPONSE(502, "CLOTH_AI_5022", "AI 서버 응답이 유효하지 않습니다."),
    AI_SERVER_RESULT_MISMATCH(502, "CLOTH_AI_5023", "AI 서버 응답 개수가 요청과 일치하지 않습니다."),
    AI_S3_DOWNLOAD_FAILED(502, "CLOTH_AI_5024", "AI 서버가 S3에서 이미지를 내려받지 못했습니다."),
    AI_S3_UPLOAD_FAILED(502, "CLOTH_AI_5025", "AI 서버가 S3에 이미지를 업로드하지 못했습니다."),
    AI_INVALID_METHOD(502, "CLOTH_AI_5026", "AI 서버에 잘못된 업로드 메서드가 전달되었습니다."),
    AI_UNEXPECTED_EXCEPTION(502, "CLOTH_AI_5027", "AI 서버에서 알 수 없는 오류가 발생했습니다."),
    AI_DETECT_EMPTY(422, "CLOTH_AI_4221", "AI 서버가 옷 객체를 인식하지 못했습니다."),
    AI_CROP_EMPTY(422, "CLOTH_AI_4222", "AI 서버가 유효한 크롭 이미지를 만들지 못했습니다.");

    private final int status;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDto getErrorReason() {
        return ErrorReasonDto.of(status, code, message);
    }
}
