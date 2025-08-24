package com.loadmapguide_backend.global.external.resilience;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 간단한 Circuit Breaker 구현
 */
@Slf4j
@Service
public class CircuitBreakerService {
    
    private static final int FAILURE_THRESHOLD = 5; // 실패 임계값
    private static final long TIMEOUT_DURATION = 60000; // 1분 (밀리초)
    
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicLong lastFailureTime = new AtomicLong(0);
    private volatile CircuitBreakerState state = CircuitBreakerState.CLOSED;
    
    public enum CircuitBreakerState {
        CLOSED,    // 정상 상태
        OPEN,      // 차단 상태 (호출 차단)
        HALF_OPEN  // 반열림 상태 (복구 시도)
    }
    
    /**
     * 카카오 API 호출 가능 여부 확인
     */
    public boolean isKakaoApiCallAllowed() {
        long currentTime = System.currentTimeMillis();
        
        switch (state) {
            case CLOSED:
                return true;
                
            case OPEN:
                if (currentTime - lastFailureTime.get() > TIMEOUT_DURATION) {
                    log.info("🔄 Circuit Breaker: OPEN -> HALF_OPEN (복구 시도)");
                    state = CircuitBreakerState.HALF_OPEN;
                    return true;
                }
                log.warn("⚡ Circuit Breaker: API 호출 차단됨 (OPEN 상태)");
                return false;
                
            case HALF_OPEN:
                return true;
                
            default:
                return false;
        }
    }
    
    /**
     * API 호출 성공 시 호출
     */
    public void recordSuccess() {
        if (state == CircuitBreakerState.HALF_OPEN) {
            log.info("✅ Circuit Breaker: HALF_OPEN -> CLOSED (복구 완료)");
            state = CircuitBreakerState.CLOSED;
        }
        failureCount.set(0);
    }
    
    /**
     * API 호출 실패 시 호출
     */
    public void recordFailure() {
        int currentFailures = failureCount.incrementAndGet();
        lastFailureTime.set(System.currentTimeMillis());
        
        log.warn("⚠️ Circuit Breaker: API 호출 실패 {}/{}", currentFailures, FAILURE_THRESHOLD);
        
        if (currentFailures >= FAILURE_THRESHOLD) {
            if (state != CircuitBreakerState.OPEN) {
                log.error("🚨 Circuit Breaker: {} -> OPEN (API 차단)", state);
                state = CircuitBreakerState.OPEN;
            }
        }
    }
    
    /**
     * 현재 상태 정보 조회
     */
    public CircuitBreakerStatus getStatus() {
        return CircuitBreakerStatus.builder()
                .state(state)
                .failureCount(failureCount.get())
                .lastFailureTime(lastFailureTime.get() > 0 ? 
                    LocalDateTime.now().minusSeconds((System.currentTimeMillis() - lastFailureTime.get()) / 1000) : null)
                .build();
    }
    
    @lombok.Builder
    public static class CircuitBreakerStatus {
        public final CircuitBreakerState state;
        public final int failureCount;
        public final LocalDateTime lastFailureTime;
    }
}