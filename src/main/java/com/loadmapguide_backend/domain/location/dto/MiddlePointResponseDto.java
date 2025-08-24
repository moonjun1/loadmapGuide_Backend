package com.loadmapguide_backend.domain.location.dto;

import com.loadmapguide_backend.global.external.weather.dto.WeatherResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MiddlePointResponseDto {
    private String message;
    private List<CandidatePoint> candidates;
    private WeatherInfo weather;
    
    @Data
    @AllArgsConstructor
    public static class CandidatePoint {
        private int rank;
        private double latitude;
        private double longitude;
        private double score;
        private String description;
        private double avgTravelTime;
        private double commercialScore;
    }
    
    @Data
    @AllArgsConstructor
    public static class WeatherInfo {
        private String condition;
        private String description;
        private Double temperature;
        private Integer humidity;
        private String cityName;
        
        public static WeatherInfo from(WeatherResponse weatherResponse) {
            if (weatherResponse == null || weatherResponse.getWeather().isEmpty()) {
                return new WeatherInfo("Unknown", "날씨 정보 없음", null, null, "알 수 없음");
            }
            
            WeatherResponse.Weather weather = weatherResponse.getWeather().get(0);
            WeatherResponse.Main main = weatherResponse.getMain();
            
            return new WeatherInfo(
                weather.getMain(),
                weather.getDescription(),
                main.getTemperature(),
                main.getHumidity(),
                weatherResponse.getCityName()
            );
        }
    }
}