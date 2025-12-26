package org.clokey.dto;

public record SuccessReasonDto(int status, String code, String message) {
    public static SuccessReasonDto of(int status, String code, String message) {
        return new SuccessReasonDto(status, code, message);
    }
}
