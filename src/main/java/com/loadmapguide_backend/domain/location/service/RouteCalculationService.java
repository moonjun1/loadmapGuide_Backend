package com.loadmapguide_backend.domain.location.service;

import com.loadmapguide_backend.domain.location.entity.LocationPoint;
import com.loadmapguide_backend.global.common.enums.TransportationType;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteCalculationService {
    
    /**
     * 두 지점 간의 경로 정보 계산
     */
    @Cacheable(value = "route-cache", key = "#origin.latitude + ':' + #origin.longitude + ':' + #destination.latitude + ':' + #destination.longitude + ':' + #transportationType")
    public RouteInfo calculateRoute(LocationPoint origin, LocationPoint destination, 
                                  TransportationType transportationType) {
        
        try {
            log.debug("경로 계산: {} -> {}, 교통수단: {}", 
                    origin.getAddress(), destination.getAddress(), transportationType);
            
            // 직선 거리 계산
            double distance = calculateDistance(origin, destination);
            
            // 교통수단별 예상 이동시간 계산
            RouteCalculation calculation = calculateTravelTime(distance, transportationType);
            
            return RouteInfo.builder()
                    .origin(origin)
                    .destination(destination)
                    .transportationType(transportationType)
                    .distanceMeters(distance)
                    .travelTimeMinutes(calculation.getTravelTimeMinutes())
                    .transferCount(calculation.getTransferCount())
                    .routeSummary(calculation.getRouteSummary())
                    .fare(calculation.getFare())
                    .build();
                    
        } catch (Exception e) {
            log.error("경로 계산 실패", e);
            
            // 기본값 반환
            return RouteInfo.builder()
                    .origin(origin)
                    .destination(destination)
                    .transportationType(transportationType)
                    .distanceMeters(calculateDistance(origin, destination))
                    .travelTimeMinutes(60) // 기본 1시간
                    .transferCount(1)
                    .routeSummary("경로 계산 실패")
                    .fare(2000)
                    .build();
        }
    }
    
    /**
     * 여러 출발지에서 목적지까지의 경로 정보들 계산
     */
    public List<RouteInfo> calculateMultipleRoutes(List<LocationPoint> origins, 
                                                  LocationPoint destination,
                                                  TransportationType transportationType) {
        
        return origins.stream()
                .map(origin -> calculateRoute(origin, destination, transportationType))
                .toList();
    }
    
    /**
     * 두 지점 간 직선 거리 계산 (Haversine formula)
     */
    private double calculateDistance(LocationPoint point1, LocationPoint point2) {
        final int R = 6371; // Earth's radius in km
        
        double latDistance = Math.toRadians(point2.getLatitude() - point1.getLatitude());
        double lonDistance = Math.toRadians(point2.getLongitude() - point1.getLongitude());
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(point1.getLatitude())) 
                * Math.cos(Math.toRadians(point2.getLatitude()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
                
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c * 1000; // Convert to meters
    }
    
    /**
     * 교통수단별 예상 이동시간 계산
     */
    private RouteCalculation calculateTravelTime(double distanceMeters, TransportationType transportationType) {
        
        return switch (transportationType) {
            case PUBLIC_TRANSPORT -> calculatePublicTransportRoute(distanceMeters);
            case CAR -> calculateCarRoute(distanceMeters);
            case WALK -> calculateWalkRoute(distanceMeters);
        };
    }
    
    /**
     * 대중교통 경로 계산
     */
    private RouteCalculation calculatePublicTransportRoute(double distanceMeters) {
        double distanceKm = distanceMeters / 1000.0;
        
        // 대중교통 평균 속도: 25km/h (환승시간 포함)
        int travelTimeMinutes = (int) Math.ceil(distanceKm / 25.0 * 60);
        
        // 거리별 환승 횟수 추정
        int transferCount = distanceKm < 5 ? 0 : (int) Math.ceil(distanceKm / 10);
        
        // 기본 요금 (서울 기준)
        int baseFare = 1540;
        int fare = baseFare + (transferCount * 200);
        
        String routeSummary = String.format("대중교통 이용 (환승 %d회 예상)", transferCount);
        
        return RouteCalculation.builder()
                .travelTimeMinutes(travelTimeMinutes)
                .transferCount(transferCount)
                .fare(fare)
                .routeSummary(routeSummary)
                .build();
    }
    
    /**
     * 자동차 경로 계산
     */
    private RouteCalculation calculateCarRoute(double distanceMeters) {
        double distanceKm = distanceMeters / 1000.0;
        
        // 시내 평균 속도: 30km/h (신호등, 교통체증 고려)
        int travelTimeMinutes = (int) Math.ceil(distanceKm / 30.0 * 60);
        
        // 주차료 + 기름값 추정
        int estimatedCost = (int) (distanceKm * 500 + 3000); // km당 500원 + 주차료 3000원
        
        String routeSummary = String.format("자동차 이용 (%.1fkm)", distanceKm);
        
        return RouteCalculation.builder()
                .travelTimeMinutes(travelTimeMinutes)
                .transferCount(0)
                .fare(estimatedCost)
                .routeSummary(routeSummary)
                .build();
    }
    
    /**
     * 도보 경로 계산
     */
    private RouteCalculation calculateWalkRoute(double distanceMeters) {
        double distanceKm = distanceMeters / 1000.0;
        
        // 도보 평균 속도: 5km/h
        int travelTimeMinutes = (int) Math.ceil(distanceKm / 5.0 * 60);
        
        String routeSummary = String.format("도보 (%.1fkm)", distanceKm);
        
        return RouteCalculation.builder()
                .travelTimeMinutes(travelTimeMinutes)
                .transferCount(0)
                .fare(0)
                .routeSummary(routeSummary)
                .build();
    }
    
    /**
     * 경로 정보
     */
    @Getter
    @Builder
    public static class RouteInfo {
        private LocationPoint origin;
        private LocationPoint destination;
        private TransportationType transportationType;
        private double distanceMeters;
        private int travelTimeMinutes;
        private int transferCount;
        private String routeSummary;
        private int fare;
    }
    
    /**
     * 경로 계산 결과
     */
    @Getter
    @Builder
    private static class RouteCalculation {
        private int travelTimeMinutes;
        private int transferCount;
        private int fare;
        private String routeSummary;
    }
}