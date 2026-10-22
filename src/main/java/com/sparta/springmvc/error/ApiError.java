package com.sparta.springmvc.error;

import java.util.List;

public record ApiError(String code, String message, List<FieldError> errors) {
    public record FieldError(String field, String message) {}
}
