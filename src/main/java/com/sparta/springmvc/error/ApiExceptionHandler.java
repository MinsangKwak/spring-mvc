package com.sparta.springmvc.error;

import java.util.List;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ApiError.FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiError.FieldError(error.getField(),
                        error.isBindingFailure() ? "입력 형식을 확인해 주세요." : error.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest().body(new ApiError("INVALID_INPUT", "입력값을 확인해 주세요.", errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> constraints(ConstraintViolationException ex) {
        List<ApiError.FieldError> errors = ex.getConstraintViolations().stream()
                .map(error -> {
                    String path = error.getPropertyPath().toString();
                    String field = path.substring(path.lastIndexOf('.') + 1);
                    return new ApiError.FieldError(field, field.equals("age")
                            ? "나이는 0부터 150 사이의 정수로 입력해 주세요."
                            : "이름은 공백만 입력할 수 없으며 최대 40자입니다.");
                }).toList();
        return ResponseEntity.badRequest().body(new ApiError("INVALID_INPUT", "입력값을 확인해 주세요.", errors));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String code = switch (status.value()) {
            case 400 -> "INVALID_REQUEST";
            case 404 -> "NOT_FOUND";
            case 405 -> "METHOD_NOT_ALLOWED";
            case 406 -> "NOT_ACCEPTABLE";
            case 415 -> "UNSUPPORTED_MEDIA_TYPE";
            default -> "REQUEST_FAILED";
        };
        String message = switch (status.value()) {
            case 400 -> "필수값, 데이터 타입, JSON 문법을 확인해 주세요.";
            case 404 -> "요청한 경로가 없습니다.";
            case 405 -> "이 경로에서 지원하지 않는 HTTP 메서드입니다.";
            case 406 -> "Accept 헤더가 이 응답 형식과 맞지 않습니다.";
            case 415 -> "Content-Type을 확인해 주세요.";
            default -> "요청을 처리하지 못했습니다.";
        };
        List<ApiError.FieldError> errors = List.of();
        if (ex instanceof MissingServletRequestParameterException missing) {
            errors = List.of(new ApiError.FieldError(missing.getParameterName(), "필수 입력값입니다."));
        } else if (ex instanceof MethodArgumentTypeMismatchException mismatch) {
            errors = List.of(new ApiError.FieldError(mismatch.getName(), "입력 형식을 확인해 주세요."));
        }
        return new ResponseEntity<>(new ApiError(code, message, errors), headers, status);
    }
}
