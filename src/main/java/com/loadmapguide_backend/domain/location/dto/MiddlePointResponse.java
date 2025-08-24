package com.loadmapguide_backend.domain.location.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MiddlePointResponse {
    
    private LocationPoint optimalLocation;
    private List<LocationPoint> candidateLocations;
    private CalculationInfo calculationInfo;
    
    @Getter
    @Builder
    public static class LocationPoint {
        private Double latitude;
        private Double longitude;
        private String address;
        private String placeName;
        private Double averageTravelTime;
        private Double commercialScore;
        private Double overallScore;
        private List<RouteInfo> routesFromStartPoints;
    }
    
    @Getter
    @Builder
    public static class RouteInfo {
        private String fromAddress;
        private Integer travelTimeMinutes;
        private Integer transferCount;
        private String routeSummary;
    }
    
    @Getter
    @Builder
    public static class CalculationInfo {
        private Integer totalParticipants;
        private String transportationType;
        private Long calculationTimeMs;
        private String algorithm;
        private Double fairnessScore;
    }
}