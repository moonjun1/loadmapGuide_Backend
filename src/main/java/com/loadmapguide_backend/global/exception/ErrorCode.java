package com.loadmapguide_backend.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    
    // Common
    INVALID_INPUT("INVALID_INPUT", "입력값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "요청한 리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    
    // Location
    INVALID_LOCATION("INVALID_LOCATION", "올바르지 않은 위치 정보입니다.", HttpStatus.BAD_REQUEST),
    LOCATION_NOT_FOUND("LOCATION_NOT_FOUND", "해당 위치를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    LOCATION_CALCULATION_FAILED("LOCATION_CALCULATION_FAILED", "중간지점 계산에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // Place
    PLACE_SEARCH_FAILED("PLACE_SEARCH_FAILED", "장소 검색에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PLACE_NOT_FOUND("PLACE_NOT_FOUND", "해당 장소를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    
    // External API
    EXTERNAL_API_ERROR("EXTERNAL_API_ERROR", "외부 API 호출 중 오류가 발생했습니다.", HttpStatus.SERVICE_UNAVAILABLE),
    API_RATE_LIMIT_EXCEEDED("API_RATE_LIMIT_EXCEEDED", "API 호출 한도를 초과했습니다.", HttpStatus.TOO_MANY_REQUESTS);
    
    private final String code;
    private final String message;
    private final HttpStatus status;
}