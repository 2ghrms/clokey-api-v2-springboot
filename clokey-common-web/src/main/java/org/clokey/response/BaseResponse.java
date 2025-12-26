package org.clokey.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDateTime;
import org.clokey.code.BaseSuccessCode;
import org.clokey.exception.BaseErrorCode;

@JsonPropertyOrder({"isSuccess", "code", "message", "timeStamp", "result"})
public record BaseResponse<T>(
        @JsonProperty("isSuccess") Boolean isSuccess,
        String code,
        String message,
        LocalDateTime timeStamp,
        @JsonInclude(JsonInclude.Include.NON_NULL) T result) {
    public static <T> BaseResponse<T> onSuccess(BaseSuccessCode code, T result) {
        return new BaseResponse<>(
                true,
                code.getReasonDto().code(),
                code.getReasonDto().message(),
                LocalDateTime.now(),
                result);
    }

    public static <T> BaseResponse<T> onFailure(BaseErrorCode code, T result) {
        return new BaseResponse<>(
                false,
                code.getErrorReason().code(),
                code.getErrorReason().message(),
                LocalDateTime.now(),
                result);
    }

    public static <T> BaseResponse<T> onFailure(String code, String message, T data) {
        return new BaseResponse<>(false, code, message, LocalDateTime.now(), data);
    }
}
