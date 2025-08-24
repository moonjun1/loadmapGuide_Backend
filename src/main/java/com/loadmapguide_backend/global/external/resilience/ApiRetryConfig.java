package com.loadmapguide_backend.global.external.resilience;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class ApiRetryConfig {
    
    /**
     * 카카오 API 호출용 Retry 템플릿
     */
    @Bean
    public RetryTemplate kakaoApiRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // 재시도 정책
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(WebClientResponseException.TooManyRequests.class, true); // 429
        retryableExceptions.put(WebClientResponseException.ServiceUnavailable.class, true); // 503
        retryableExceptions.put(WebClientResponseException.BadGateway.class, true); // 502
        retryableExceptions.put(WebClientResponseException.GatewayTimeout.class, true); // 504
        
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3, retryableExceptions);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        // 백오프 정책 (지수적 증가)
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000); // 1초
        backOffPolicy.setMultiplier(2.0); // 2배씩 증가
        backOffPolicy.setMaxInterval(10000); // 최대 10초
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        // 재시도 리스너
        retryTemplate.registerListener(new ApiRetryListener());
        
        return retryTemplate;
    }
}