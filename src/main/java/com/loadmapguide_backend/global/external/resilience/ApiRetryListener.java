package com.loadmapguide_backend.global.external.resilience;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ApiRetryListener extends RetryListenerSupport {
    
    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        log.warn("🔄 API 호출 재시도 {}/{} - 오류: {}", 
                context.getRetryCount(), 
                context.getAttribute(RetryContext.MAX_ATTEMPTS), 
                throwable.getMessage());
    }
    
    @Override
    public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
        log.debug("🚀 API 호출 시작");
        return super.open(context, callback);
    }
    
    @Override
    public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        if (throwable != null) {
            log.error("❌ API 호출 최종 실패 after {} 회 재시도: {}", 
                    context.getRetryCount(), throwable.getMessage());
        } else {
            if (context.getRetryCount() > 0) {
                log.info("✅ API 호출 성공 after {} 회 재시도", context.getRetryCount());
            } else {
                log.debug("✅ API 호출 성공");
            }
        }
        super.close(context, callback, throwable);
    }
}