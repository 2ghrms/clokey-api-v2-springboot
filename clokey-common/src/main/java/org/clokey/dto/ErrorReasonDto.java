package org.clokey.dto;

public record ErrorReasonDto(int status, String code, String message) {
    public static ErrorReasonDto of(int status, String code, String message) {
        return new ErrorReasonDto(status, code, message);
    }
}
