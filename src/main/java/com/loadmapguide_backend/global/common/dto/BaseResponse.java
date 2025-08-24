package com.loadmapguide_backend.global.common.dto;

import lombok.Getter;

@Getter
public class BaseResponse<T> {
    private final boolean success;
    private final String message;
    private final T data;
    
    private BaseResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(true, "요청이 성공적으로 처리되었습니다.", data);
    }
    
    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>(true, message, data);
    }
    
    public static BaseResponse<Void> success() {
        return new BaseResponse<>(true, "요청이 성공적으로 처리되었습니다.", null);
    }
    
    public static BaseResponse<Void> error(String message) {
        return new BaseResponse<>(false, message, null);
    }
}