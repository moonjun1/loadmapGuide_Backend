package com.loadmapguide_backend.global.external.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class KakaoDirectionResponse {
    
    @JsonProperty("routes")
    private List<Route> routes;
    
    @Getter
    @NoArgsConstructor
    public static class Route {
        @JsonProperty("result_code")
        private Integer resultCode;
        
        @JsonProperty("result_msg")
        private String resultMsg;
        
        @JsonProperty("summary")
        private Summary summary;
        
        @JsonProperty("sections")
        private List<Section> sections;
        
        @Getter
        @NoArgsConstructor
        public static class Summary {
            @JsonProperty("origin")
            private Location origin;
            
            @JsonProperty("destination")
            private Location destination;
            
            @JsonProperty("waypoints")
            private List<Location> waypoints;
            
            @JsonProperty("priority")
            private String priority;
            
            @JsonProperty("bound")
            private Bound bound;
            
            @JsonProperty("fare")
            private Fare fare;
            
            @JsonProperty("distance")
            private Integer distance; // 미터 단위
            
            @JsonProperty("duration")
            private Integer duration; // 초 단위
        }
        
        @Getter
        @NoArgsConstructor
        public static class Section {
            @JsonProperty("distance")
            private Integer distance;
            
            @JsonProperty("duration")
            private Integer duration;
            
            @JsonProperty("bound")
            private Bound bound;
            
            @JsonProperty("roads")
            private List<Road> roads;
            
            @JsonProperty("guides")
            private List<Guide> guides;
        }
        
        @Getter
        @NoArgsConstructor
        public static class Location {
            @JsonProperty("x")
            private Double longitude;
            
            @JsonProperty("y")
            private Double latitude;
        }
        
        @Getter
        @NoArgsConstructor
        public static class Bound {
            @JsonProperty("min_x")
            private Double minX;
            
            @JsonProperty("min_y")
            private Double minY;
            
            @JsonProperty("max_x")
            private Double maxX;
            
            @JsonProperty("max_y")
            private Double maxY;
        }
        
        @Getter
        @NoArgsConstructor
        public static class Fare {
            @JsonProperty("taxi")
            private Integer taxi;
            
            @JsonProperty("toll")
            private Integer toll;
        }
        
        @Getter
        @NoArgsConstructor
        public static class Road {
            @JsonProperty("name")
            private String name;
            
            @JsonProperty("distance")
            private Integer distance;
            
            @JsonProperty("duration")
            private Integer duration;
            
            @JsonProperty("traffic_speed")
            private Double trafficSpeed;
            
            @JsonProperty("traffic_state")
            private Integer trafficState; // 0:정보없음, 1:원활, 2:서행, 3:지체, 4:정체
            
            @JsonProperty("vertexes")
            private List<Double> vertexes;
        }
        
        @Getter
        @NoArgsConstructor
        public static class Guide {
            @JsonProperty("name")
            private String name;
            
            @JsonProperty("x")
            private Double longitude;
            
            @JsonProperty("y")
            private Double latitude;
            
            @JsonProperty("distance")
            private Integer distance;
            
            @JsonProperty("duration")
            private Integer duration;
            
            @JsonProperty("type")
            private Integer type;
            
            @JsonProperty("guidance")
            private String guidance;
            
            @JsonProperty("road_index")
            private Integer roadIndex;
        }
    }
    
    /**
     * 성공적인 응답 여부 확인
     */
    public boolean isSuccess() {
        return routes != null && !routes.isEmpty() && 
               routes.get(0).getResultCode() != null && 
               routes.get(0).getResultCode() == 0;
    }
    
    /**
     * 첫 번째 경로 정보 반환
     */
    public Route getFirstRoute() {
        return routes != null && !routes.isEmpty() ? routes.get(0) : null;
    }
    
    /**
     * 총 거리 (미터)
     */
    public Integer getTotalDistance() {
        Route route = getFirstRoute();
        return route != null && route.getSummary() != null ? 
               route.getSummary().getDistance() : 0;
    }
    
    /**
     * 총 소요시간 (초)
     */
    public Integer getTotalDuration() {
        Route route = getFirstRoute();
        return route != null && route.getSummary() != null ? 
               route.getSummary().getDuration() : 0;
    }
    
    /**
     * 총 소요시간 (분)
     */
    public Integer getTotalDurationInMinutes() {
        return getTotalDuration() / 60;
    }
    
    /**
     * 택시 요금
     */
    public Integer getTaxiFare() {
        Route route = getFirstRoute();
        return route != null && route.getSummary() != null && 
               route.getSummary().getFare() != null ? 
               route.getSummary().getFare().getTaxi() : 0;
    }
    
    /**
     * 통행료
     */
    public Integer getTollFare() {
        Route route = getFirstRoute();
        return route != null && route.getSummary() != null && 
               route.getSummary().getFare() != null ? 
               route.getSummary().getFare().getToll() : 0;
    }
}