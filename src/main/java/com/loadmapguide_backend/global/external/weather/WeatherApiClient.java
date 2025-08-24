package com.loadmapguide_backend.global.external.weather;

import com.loadmapguide_backend.global.external.weather.dto.WeatherResponse;
import com.loadmapguide_backend.global.exception.BusinessException;
import com.loadmapguide_backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherApiClient {
    
    private final WebClient weatherWebClient;
    
    @Value("${external-api.weather.api-key}")
    private String weatherApiKey;
    
    @Cacheable(value = "weather", key = "#latitude + ':' + #longitude")
    public WeatherResponse getCurrentWeather(double latitude, double longitude) {
        if (isApiKeyNotConfigured()) {
            log.warn("날씨 API 키가 설정되지 않았습니다. 기본값을 반환합니다.");
            return createDefaultWeatherResponse();
        }
        
        try {
            log.debug("날씨 정보 요청: lat={}, lon={}", latitude, longitude);
            
            WeatherResponse response = weatherWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/weather")
                            .queryParam("lat", latitude)
                            .queryParam("lon", longitude)
                            .queryParam("appid", weatherApiKey)
                            .queryParam("units", "metric")
                            .queryParam("lang", "kr")
                            .build())
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), clientResponse -> {
                        log.error("날씨 API 클라이언트 오류: {}", clientResponse.statusCode());
                        return Mono.error(new BusinessException(ErrorCode.EXTERNAL_API_ERROR, 
                                "날씨 API 요청이 잘못되었습니다"));
                    })
                    .onStatus(status -> status.is5xxServerError(), clientResponse -> {
                        log.error("날씨 API 서버 오류: {}", clientResponse.statusCode());
                        return Mono.error(new BusinessException(ErrorCode.EXTERNAL_API_ERROR, 
                                "날씨 API 서버 오류"));
                    })
                    .bodyToMono(WeatherResponse.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            
            log.debug("날씨 정보 조회 성공: {}", response.getCityName());
            return response;
            
        } catch (Exception e) {
            log.error("날씨 정보 조회 실패 - lat: {}, lon: {}", latitude, longitude, e);
            
            // 외부 API 실패 시 기본 날씨 정보 반환
            log.info("기본 날씨 정보를 반환합니다.");
            return createDefaultWeatherResponse();
        }
    }
    
    private boolean isApiKeyNotConfigured() {
        return weatherApiKey == null || 
               weatherApiKey.trim().isEmpty() || 
               "your-weather-api-key".equals(weatherApiKey);
    }
    
    private WeatherResponse createDefaultWeatherResponse() {
        WeatherResponse defaultResponse = new WeatherResponse();
        defaultResponse.setCityName("서울");
        
        WeatherResponse.Weather weather = new WeatherResponse.Weather();
        weather.setMain("Clear");
        weather.setDescription("맑음");
        weather.setIcon("01d");
        defaultResponse.setWeather(java.util.List.of(weather));
        
        WeatherResponse.Main main = new WeatherResponse.Main();
        main.setTemperature(20.0);
        main.setFeelsLike(20.0);
        main.setHumidity(60);
        main.setPressure(1013);
        defaultResponse.setMain(main);
        
        WeatherResponse.Wind wind = new WeatherResponse.Wind();
        wind.setSpeed(2.0);
        wind.setDirection(180);
        defaultResponse.setWind(wind);
        
        return defaultResponse;
    }
}