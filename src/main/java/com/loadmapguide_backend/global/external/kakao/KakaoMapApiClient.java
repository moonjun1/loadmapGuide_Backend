package com.loadmapguide_backend.global.external.kakao;

import com.loadmapguide_backend.global.exception.BusinessException;
import com.loadmapguide_backend.global.exception.ErrorCode;
import com.loadmapguide_backend.global.external.kakao.dto.KakaoCoordinateResponse;
import com.loadmapguide_backend.global.external.kakao.dto.KakaoPlaceResponse;
import com.loadmapguide_backend.global.external.resilience.CircuitBreakerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoMapApiClient {
    
    @Qualifier("kakaoWebClient")
    private final WebClient kakaoWebClient;
    private final RetryTemplate kakaoApiRetryTemplate;
    private final CircuitBreakerService circuitBreakerService;
    
    /**
     * 주소를 좌표로 변환 (지오코딩) - 향상된 에러 처리 및 Circuit Breaker 적용
     */
    @Cacheable(value = "geocoding", key = "#address")
    public KakaoCoordinateResponse getCoordinateByAddress(String address) {
        // Circuit Breaker 확인
        if (!circuitBreakerService.isKakaoApiCallAllowed()) {
            log.warn("🚨 Circuit Breaker가 열려있음 - 카카오 지오코딩 API 호출 차단");
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "카카오 API 서비스가 일시적으로 중단되었습니다.");
        }
        
        try {
            log.debug("🌍 카카오 지오코딩 API 호출: {}", address);
            
            return kakaoApiRetryTemplate.execute(context -> {
                try {
                    KakaoCoordinateResponse response = kakaoWebClient.get()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/v2/local/search/address.json")
                                    .queryParam("query", address)
                                    .queryParam("analyze_type", "similar") // 유사 주소 분석
                                    .build())
                            .retrieve()
                            .bodyToMono(KakaoCoordinateResponse.class)
                            .timeout(Duration.ofSeconds(10))
                            .doOnSuccess(res -> {
                                log.debug("✅ 지오코딩 성공: {} -> 결과 {}개", address, 
                                        res.getMeta().getTotalCount());
                                circuitBreakerService.recordSuccess();
                            })
                            .doOnError(error -> {
                                log.error("❌ 지오코딩 실패: {}", address, error);
                                circuitBreakerService.recordFailure();
                            })
                            .block();
                    
                    return response;
                    
                } catch (WebClientResponseException e) {
                    handleWebClientException(e, "지오코딩");
                    throw e; // RetryTemplate에서 재시도 판단
                }
            });
            
        } catch (WebClientResponseException e) {
            return handleApiError(e, "지오코딩", address);
        } catch (Exception e) {
            log.error("❌ 카카오 지오코딩 API 호출 중 예외 발생: {}", address, e);
            circuitBreakerService.recordFailure();
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "주소 변환 서비스가 일시적으로 사용할 수 없습니다.");
        }
    }
    
    /**
     * 좌표를 주소로 변환 (역지오코딩)
     */
    @Cacheable(value = "geocoding", key = "'reverse:' + #longitude + ':' + #latitude")
    public KakaoCoordinateResponse getAddressByCoordinate(Double longitude, Double latitude) {
        try {
            log.debug("카카오 역지오코딩 API 호출: ({}, {})", latitude, longitude);
            
            return kakaoWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/local/geo/coord2address.json")
                            .queryParam("x", longitude)
                            .queryParam("y", latitude)
                            .build())
                    .retrieve()
                    .bodyToMono(KakaoCoordinateResponse.class)
                    .timeout(Duration.ofSeconds(5))
                    .retryWhen(Retry.backoff(2, Duration.ofMillis(500)))
                    .doOnSuccess(response -> log.debug("역지오코딩 성공: ({}, {}) -> 결과 {}개", 
                            latitude, longitude, response.getMeta().getTotalCount()))
                    .doOnError(error -> log.error("역지오코딩 실패: ({}, {})", latitude, longitude, error))
                    .block();
                    
        } catch (Exception e) {
            log.error("카카오 역지오코딩 API 호출 중 예외 발생", e);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, e);
        }
    }
    
    /**
     * 키워드로 장소 검색
     */
    @Cacheable(value = "places", key = "'keyword:' + #keyword + ':' + #longitude + ':' + #latitude + ':' + #radius")
    public KakaoPlaceResponse searchPlacesByKeyword(String keyword, Double longitude, Double latitude, Integer radius) {
        try {
            log.debug("카카오 키워드 검색 API 호출: keyword={}, center=({}, {}), radius={}", 
                    keyword, latitude, longitude, radius);
            
            return kakaoWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/local/search/keyword.json")
                            .queryParam("query", keyword)
                            .queryParam("x", longitude)
                            .queryParam("y", latitude)
                            .queryParam("radius", radius)
                            .queryParam("size", 15)
                            .queryParam("sort", "distance")
                            .build())
                    .retrieve()
                    .bodyToMono(KakaoPlaceResponse.class)
                    .timeout(Duration.ofSeconds(5))
                    .retryWhen(Retry.backoff(2, Duration.ofMillis(500)))
                    .doOnSuccess(response -> log.debug("키워드 검색 성공: {} -> 결과 {}개", 
                            keyword, response.getMeta().getTotalCount()))
                    .doOnError(error -> log.error("키워드 검색 실패: {}", keyword, error))
                    .block();
                    
        } catch (Exception e) {
            log.error("카카오 키워드 검색 API 호출 중 예외 발생", e);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, e);
        }
    }
    
    /**
     * 카테고리로 장소 검색
     */
    @Cacheable(value = "places", key = "'category:' + #categoryCode + ':' + #longitude + ':' + #latitude + ':' + #radius")
    public KakaoPlaceResponse searchPlacesByCategory(String categoryCode, Double longitude, Double latitude, Integer radius) {
        try {
            log.debug("카카오 카테고리 검색 API 호출: category={}, center=({}, {}), radius={}", 
                    categoryCode, latitude, longitude, radius);
            
            return kakaoWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/local/search/category.json")
                            .queryParam("category_group_code", categoryCode)
                            .queryParam("x", longitude)
                            .queryParam("y", latitude)
                            .queryParam("radius", radius)
                            .queryParam("size", 15)
                            .queryParam("sort", "distance")
                            .build())
                    .retrieve()
                    .bodyToMono(KakaoPlaceResponse.class)
                    .timeout(Duration.ofSeconds(5))
                    .retryWhen(Retry.backoff(2, Duration.ofMillis(500)))
                    .doOnSuccess(response -> log.debug("카테고리 검색 성공: {} -> 결과 {}개", 
                            categoryCode, response.getMeta().getTotalCount()))
                    .doOnError(error -> log.error("카테고리 검색 실패: {}", categoryCode, error))
                    .block();
                    
        } catch (Exception e) {
            log.error("카카오 카테고리 검색 API 호출 중 예외 발생", e);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, e);
        }
    }
    
    /**
     * WebClient 예외 처리
     */
    private void handleWebClientException(WebClientResponseException e, String apiType) {
        int statusCode = e.getStatusCode().value();
        String responseBody = e.getResponseBodyAsString();
        
        log.error("🚨 카카오 {} API 오류: status={}, body={}", apiType, statusCode, responseBody);
        
        switch (statusCode) {
            case 400 -> log.error("❌ 잘못된 요청 파라미터: {}", responseBody);
            case 401 -> log.error("❌ 인증 실패: API 키를 확인하세요");
            case 429 -> log.warn("⚠️ API 호출 한도 초과 - 잠시 후 재시도");
            case 500, 502, 503, 504 -> log.error("❌ 카카오 서버 오류: status={}", statusCode);
            default -> log.error("❌ 알 수 없는 오류: status={}", statusCode);
        }
        
        circuitBreakerService.recordFailure();
    }
    
    /**
     * API 에러 처리 및 적절한 예외 변환
     */
    private <T> T handleApiError(WebClientResponseException e, String apiType, String query) {
        int statusCode = e.getStatusCode().value();
        
        switch (statusCode) {
            case 400:
                throw new BusinessException(ErrorCode.INVALID_INPUT, 
                        "검색 조건이 올바르지 않습니다: " + query);
            case 401:
                throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, 
                        "API 인증에 실패했습니다. 관리자에게 문의하세요.");
            case 429:
                throw new BusinessException(ErrorCode.API_RATE_LIMIT_EXCEEDED, 
                        "API 호출 한도를 초과했습니다. 잠시 후 다시 시도하세요.");
            case 500:
            case 502:
            case 503:
            case 504:
                throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, 
                        "외부 서비스가 일시적으로 사용할 수 없습니다. 잠시 후 다시 시도하세요.");
            default:
                throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, 
                        "외부 API 호출 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * API 응답 유효성 검증
     */
    private boolean isValidResponse(KakaoCoordinateResponse response) {
        return response != null && 
               response.getMeta() != null && 
               response.getDocuments() != null && 
               !response.getDocuments().isEmpty();
    }
    
    private boolean isValidResponse(KakaoPlaceResponse response) {
        return response != null && 
               response.getMeta() != null && 
               response.getDocuments() != null && 
               !response.getDocuments().isEmpty();
    }
}