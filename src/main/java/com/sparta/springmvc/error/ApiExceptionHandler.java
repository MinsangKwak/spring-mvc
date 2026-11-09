package com.sparta.springmvc.error;

import java.util.List;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 모든 API 오류를 {@link ApiError} 한 가지 형식으로 바꾸는 유일한 지점.
 * <p>
 * 오류가 어느 단계에서 났는지에 따라 code가 갈린다.
 * <ul>
 *   <li>INVALID_INPUT: 객체가 만들어진 뒤 제약(@NotBlank·@Min 등)에 걸림. 필드별 errors 포함.
 *       {@code @ModelAttribute}는 타입 변환 실패도 BindingResult에 담기므로 여기로 온다.</li>
 *   <li>INVALID_REQUEST: 객체를 만들기 전에 실패. JSON 문법·타입(HttpMessageNotReadable),
 *       {@code @RequestParam} 누락·타입 불일치. 필드 정보가 없을 수 있다.</li>
 *   <li>NOT_FOUND·METHOD_NOT_ALLOWED·NOT_ACCEPTABLE·UNSUPPORTED_MEDIA_TYPE: 핸들러 매핑 단계의 HTTP 오류.</li>
 *   <li>INTERNAL_ERROR: 위 어디에도 해당하지 않는 예외. 로그에 남기고 500을 돌려준다.</li>
 * </ul>
 * 부모 {@link ResponseEntityExceptionHandler}가 Spring MVC 표준 예외를 상태 코드로 분류하고,
 * 이 클래스는 그 결과를 {@link #handleExceptionInternal}에서 ApiError 본문으로 바꾼다.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    // @Valid @RequestBody · @Valid @ModelAttribute의 BindingResult 오류
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ApiError.FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiError.FieldError(error.getField(),
                        error.isBindingFailure() ? "입력 형식을 확인해 주세요." : error.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest().body(new ApiError("INVALID_INPUT", "입력값을 확인해 주세요.", errors));
    }

    // @Validated 컨트롤러의 @PathVariable·@RequestParam 개별 인자 제약 위반
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

    // 2026-11-09 위 두 경우와 Spring MVC 표준 예외에 해당하지 않는 예외의 공통 출구. 삼키지 않고 로그에 남긴다.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception ex) {
        log.error("처리하지 못한 예외", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError("INTERNAL_ERROR", "서버에서 요청을 처리하지 못했습니다.", List.of()));
    }

    // 부모가 분류한 상태 코드를 ApiError 본문으로 변환. 400·404·405·406·415 외는 REQUEST_FAILED
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
