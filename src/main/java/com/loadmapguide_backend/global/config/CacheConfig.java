package com.loadmapguide_backend.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    @Profile("dev")
    @Primary
    public CacheManager simpleCacheManager() {
        log.info("개발환경용 Simple Cache Manager를 설정합니다.");
        
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(
            "geocoding",           // 지오코딩 결과 캐시 (주소 → 좌표)
            "places",             // 장소 검색 결과 캐시  
            "weather",            // 날씨 정보 캐시 (좌표별)
            "middlePoints",       // 중간지점 계산 결과 캐시
            "routes"              // 경로 계산 결과 캐시
        );
        
        cacheManager.setAllowNullValues(false);
        
        log.info("캐시 영역이 설정되었습니다: {}", cacheManager.getCacheNames());
        return cacheManager;
    }
    
    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        log.info("Redis Template을 설정합니다.");
        
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }
    
    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        log.info("Redis Cache Manager를 설정합니다.");
        
        // 캐시별 개별 TTL 설정
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 지오코딩: 1일 (변경 가능성이 낮음)
        cacheConfigurations.put("geocoding", 
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofDays(1)));
        
        // 날씨: 10분 (자주 변경됨)
        cacheConfigurations.put("weather", 
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10)));
        
        // 장소 검색: 30분 (중간 정도)
        cacheConfigurations.put("places", 
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(30)));
        
        // 중간지점 계산: 1시간 (계산 비용이 높음)
        cacheConfigurations.put("middlePoints", 
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1)));
        
        // 경로 계산: 30분 (교통상황 변동)
        cacheConfigurations.put("routes", 
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(30)));
        
        RedisCacheConfiguration defaultConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .disableCachingNullValues();
                
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfiguration)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}