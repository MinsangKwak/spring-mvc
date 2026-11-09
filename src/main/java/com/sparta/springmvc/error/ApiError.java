package com.sparta.springmvc.error;

import java.util.List;

/**
 * 모든 API 오류 응답의 단일 본문 형식. 화면(lab.js)은 code로 종류를, errors[].field로 입력칸을 찾는다.
 * <p>
 * code: INVALID_INPUT · INVALID_REQUEST · NOT_FOUND · METHOD_NOT_ALLOWED · NOT_ACCEPTABLE
 * · UNSUPPORTED_MEDIA_TYPE · REQUEST_FAILED · INTERNAL_ERROR. 필드 추가·이름 변경 시 lab.js와 OpenAPI 스키마가 함께 바뀐다.
 */
public record ApiError(String code, String message, List<FieldError> errors) {
    public record FieldError(String field, String message) {}
}
