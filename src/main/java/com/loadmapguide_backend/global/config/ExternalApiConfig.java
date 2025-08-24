package com.loadmapguide_backend.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;

@Slf4j
@Configuration
public class ExternalApiConfig {
    
    @Value("${external-api.kakao.rest-api-key}")
    private String kakaoApiKey;
    
    @Value("${external-api.kakao.base-url}")
    private String kakaoBaseUrl;
    
    @Value("${external-api.weather.api-key}")
    private String weatherApiKey;
    
    @Value("${external-api.weather.base-url}")
    private String weatherBaseUrl;
    
    /**
     * API 키 유효성 검증
     */
    @PostConstruct
    public void validateApiKeys() {
        // 디버깅: 현재 값 출력
        log.info("🔍 DEBUG - 카카오 API 키 값: '{}'", kakaoApiKey);
        log.info("🔍 DEBUG - 환경변수 KAKAO_REST_API_KEY: '{}'", System.getenv("KAKAO_REST_API_KEY"));
        
        // 카카오 API 키 검증
        if (kakaoApiKey == null || kakaoApiKey.trim().isEmpty() || 
            "your-kakao-api-key".equals(kakaoApiKey)) {
            log.warn("⚠️ 카카오 API 키가 설정되지 않았습니다. 환경변수 KAKAO_REST_API_KEY를 설정해주세요.");
            log.info("💡 카카오 개발자 사이트: https://developers.kakao.com");
        } else {
            log.info("✅ 카카오 API 키가 설정되었습니다. (키 길이: {}자)", kakaoApiKey.length());
        }
        
        // 날씨 API 키 검증 (선택적)
        if (weatherApiKey == null || weatherApiKey.trim().isEmpty() || 
            "your-weather-api-key".equals(weatherApiKey)) {
            log.info("ℹ️ 날씨 API 키가 설정되지 않았습니다. (선택적 기능)");
        } else {
            log.info("✅ 날씨 API 키가 설정되었습니다.");
        }
        
        log.info("🔧 외부 API 설정 완료 - 카카오: {}, 날씨: {}", 
                kakaoBaseUrl, weatherBaseUrl);
    }
    
    @Bean("kakaoWebClient")
    public WebClient kakaoWebClient() {
        return WebClient.builder()
                .baseUrl(kakaoBaseUrl)
                .defaultHeader("Authorization", "KakaoAK " + kakaoApiKey)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("User-Agent", "LoadMapGuide-Backend/1.0")
                .codecs(configurer -> {
                    configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024); // 2MB
                    configurer.defaultCodecs().enableLoggingRequestDetails(true);
                })
                .build();
    }
    
    @Bean("weatherWebClient")
    public WebClient weatherWebClient() {
        return WebClient.builder()
                .baseUrl(weatherBaseUrl)
                .defaultHeader("Content-Type", "application/json")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }
}