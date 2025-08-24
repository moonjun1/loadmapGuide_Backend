package com.loadmapguide_backend.global.external.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class WeatherResponse {
    
    @JsonProperty("weather")
    private List<Weather> weather;
    
    @JsonProperty("main")
    private Main main;
    
    @JsonProperty("wind")
    private Wind wind;
    
    @JsonProperty("name")
    private String cityName;
    
    @Data
    public static class Weather {
        @JsonProperty("main")
        private String main;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("icon")
        private String icon;
    }
    
    @Data
    public static class Main {
        @JsonProperty("temp")
        private Double temperature;
        
        @JsonProperty("feels_like")
        private Double feelsLike;
        
        @JsonProperty("humidity")
        private Integer humidity;
        
        @JsonProperty("pressure")
        private Integer pressure;
    }
    
    @Data
    public static class Wind {
        @JsonProperty("speed")
        private Double speed;
        
        @JsonProperty("deg")
        private Integer direction;
    }
}