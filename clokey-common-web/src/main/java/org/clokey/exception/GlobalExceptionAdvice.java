package org.clokey.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.clokey.response.BaseResponse;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice(annotations = {RestController.class})
public class GlobalExceptionAdvice extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException e,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        String errorMessage = e.getPropertyName() + ": 올바른 값이 아닙니다.";
        return handleExceptionInternalMessage(e, headers, request, errorMessage);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException e,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        String errorMessage = e.getParameterName() + ": 올바른 값이 아닙니다.";
        return handleExceptionInternalMessage(e, headers, request, errorMessage);
    }

    @ExceptionHandler
    public ResponseEntity<Object> validation(ConstraintViolationException e, WebRequest request) {
        String errorMessage =
                e.getConstraintViolations().stream()
                        .map(constraintViolation -> constraintViolation.getMessage())
                        .findFirst()
                        .orElse("잘못된 요청입니다");

        return handleExceptionInternalConstraint(e, errorMessage, HttpHeaders.EMPTY, request);
    }

    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();

        // 필드 에러 처리
        e.getBindingResult()
                .getFieldErrors()
                .forEach(
                        fieldError -> {
                            String rawFieldName =
                                    fieldError.getField(); // ex: content[0].clothImageUrl
                            String simplifiedFieldName =
                                    rawFieldName
                                            .substring(rawFieldName.lastIndexOf('.') + 1)
                                            .replaceAll(".*\\.", ""); // → clothImageUrl

                            String errorMessage =
                                    GlobalBaseErrorCode.findByCode(fieldError.getDefaultMessage())
                                            .map(GlobalBaseErrorCode::getMessage)
                                            .orElse(fieldError.getDefaultMessage());

                            errors.merge(
                                    simplifiedFieldName,
                                    errorMessage,
                                    (existing, replacement) -> existing + ", " + replacement);
                        });

        // 클래스 레벨 에러 처리
        e.getBindingResult()
                .getGlobalErrors()
                .forEach(
                        objectError -> {
                            String errorMessage =
                                    GlobalBaseErrorCode.findByCode(objectError.getDefaultMessage())
                                            .map(GlobalBaseErrorCode::getMessage)
                                            .orElse(objectError.getDefaultMessage());

                            errors.merge(
                                    "message",
                                    errorMessage,
                                    (existing, replacement) -> existing + ", " + replacement);
                        });

        return handleExceptionInternalArgs(
                e, headers, GlobalBaseErrorCode.BAD_REQUEST, request, errors);
    }

    @ExceptionHandler
    public ResponseEntity<Object> exception(Exception e, WebRequest request) {
        e.printStackTrace();
        return handleExceptionInternalFalse(
                e,
                GlobalBaseErrorCode.INTERNAL_SERVER_ERROR,
                HttpHeaders.EMPTY,
                HttpStatus.valueOf(GlobalBaseErrorCode.INTERNAL_SERVER_ERROR.getStatus()),
                request,
                e.getMessage());
    }

    @ExceptionHandler(BaseCustomException.class)
    public ResponseEntity<Object> onThrowException(
            BaseCustomException baseCustomException, HttpServletRequest request) {
        return handleExceptionInternal(
                baseCustomException, baseCustomException.getCode(), null, request);
    }

    private ResponseEntity<Object> handleExceptionInternal(
            Exception e, BaseErrorCode code, HttpHeaders headers, HttpServletRequest request) {

        BaseResponse<Object> body = BaseResponse.onFailure(code, null);
        WebRequest webRequest = new ServletWebRequest(request);

        return super.handleExceptionInternal(
                e, body, headers, HttpStatus.valueOf(code.getErrorReason().status()), webRequest);
    }

    private ResponseEntity<Object> handleExceptionInternalFalse(
            Exception e,
            GlobalBaseErrorCode errorCommonStatus,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request,
            String errorPoint) {

        BaseResponse<Object> body = BaseResponse.onFailure(errorCommonStatus, errorPoint);
        return super.handleExceptionInternal(e, body, headers, status, request);
    }

    private ResponseEntity<Object> handleExceptionInternalArgs(
            Exception e,
            HttpHeaders headers,
            GlobalBaseErrorCode errorCommonStatus,
            WebRequest request,
            Map<String, String> errorArgs) {

        BaseResponse<Object> body = BaseResponse.onFailure(errorCommonStatus, errorArgs);
        return super.handleExceptionInternal(
                e, body, headers, HttpStatus.valueOf(errorCommonStatus.getStatus()), request);
    }

    private ResponseEntity<Object> handleExceptionInternalConstraint(
            Exception e, String errorMessage, HttpHeaders headers, WebRequest request) {

        BaseResponse<Object> body =
                BaseResponse.onFailure(
                        GlobalBaseErrorCode.BAD_REQUEST.getCode(), errorMessage, null);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                HttpStatus.valueOf(GlobalBaseErrorCode.BAD_REQUEST.getStatus()),
                request);
    }

    private ResponseEntity<Object> handleExceptionInternalMessage(
            Exception e, HttpHeaders headers, WebRequest request, String errorMessage) {

        GlobalBaseErrorCode errorStatus = GlobalBaseErrorCode.BAD_REQUEST;
        BaseResponse<String> body = BaseResponse.onFailure(errorStatus, errorMessage);

        return super.handleExceptionInternal(
                e, body, headers, HttpStatus.valueOf(errorStatus.getStatus()), request);
    }
}
