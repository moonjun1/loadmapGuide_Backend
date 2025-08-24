package com.loadmapguide_backend.global.health;

import com.loadmapguide_backend.global.common.dto.BaseResponse;
import com.loadmapguide_backend.global.external.resilience.CircuitBreakerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class ExternalApiHealthController {
    
    private final CircuitBreakerService circuitBreakerService;
    private final WebClient kakaoWebClient;
    
    @Value("${external-api.kakao.rest-api-key}")
    private String kakaoApiKey;
    
    /**
     * 외부 API 상태 확인
     */
    @GetMapping("/external-apis")
    public BaseResponse<Map<String, Object>> checkExternalApiHealth() {
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("timestamp", LocalDateTime.now());
        healthStatus.put("kakaoApi", checkKakaoApiHealth());
        healthStatus.put("circuitBreaker", getCircuitBreakerStatus());
        
        return BaseResponse.success("외부 API 상태 조회 완료", healthStatus);
    }
    
    /**
     * 카카오 API 상태 확인
     */
    private Map<String, Object> checkKakaoApiHealth() {
        Map<String, Object> kakaoStatus = new HashMap<>();
        
        try {
            // API 키 설정 확인
            boolean hasApiKey = kakaoApiKey != null && 
                               !kakaoApiKey.trim().isEmpty() && 
                               !"your-kakao-api-key".equals(kakaoApiKey);
            
            kakaoStatus.put("apiKeyConfigured", hasApiKey);
            
            if (!hasApiKey) {
                kakaoStatus.put("status", "MISSING_API_KEY");
                kakaoStatus.put("message", "카카오 API 키가 설정되지 않았습니다");
                return kakaoStatus;
            }
            
            // Circuit Breaker 상태 확인
            boolean circuitOpen = !circuitBreakerService.isKakaoApiCallAllowed();
            if (circuitOpen) {
                kakaoStatus.put("status", "CIRCUIT_OPEN");
                kakaoStatus.put("message", "Circuit Breaker가 열려있음 (API 호출 차단 중)");
                return kakaoStatus;
            }
            
            // 간단한 API 테스트 (테스트용 요청)
            try {
                kakaoWebClient.get()
                        .uri("/v2/local/search/keyword.json?query=test&size=1")
                        .retrieve()
                        .bodyToMono(String.class)
                        .timeout(java.time.Duration.ofSeconds(3))
                        .block();
                
                kakaoStatus.put("status", "HEALTHY");
                kakaoStatus.put("message", "카카오 API 정상 동작");
                circuitBreakerService.recordSuccess();
                
            } catch (Exception e) {
                kakaoStatus.put("status", "UNHEALTHY");
                kakaoStatus.put("message", "카카오 API 호출 실패: " + e.getMessage());
                log.warn("카카오 API 헬스체크 실패", e);
            }
            
        } catch (Exception e) {
            kakaoStatus.put("status", "ERROR");
            kakaoStatus.put("message", "카카오 API 상태 확인 중 오류: " + e.getMessage());
            log.error("카카오 API 헬스체크 중 예외 발생", e);
        }
        
        return kakaoStatus;
    }
    
    /**
     * Circuit Breaker 상태 정보
     */
    private Map<String, Object> getCircuitBreakerStatus() {
        CircuitBreakerService.CircuitBreakerStatus status = circuitBreakerService.getStatus();
        
        Map<String, Object> cbStatus = new HashMap<>();
        cbStatus.put("state", status.state.toString());
        cbStatus.put("failureCount", status.failureCount);
        cbStatus.put("lastFailureTime", status.lastFailureTime);
        cbStatus.put("callAllowed", circuitBreakerService.isKakaoApiCallAllowed());
        
        return cbStatus;
    }
}