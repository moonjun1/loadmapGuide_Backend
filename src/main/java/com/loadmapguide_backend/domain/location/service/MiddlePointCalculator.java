package com.loadmapguide_backend.domain.location.service;

import com.loadmapguide_backend.domain.location.dto.LocationRequest;
import com.loadmapguide_backend.domain.location.dto.MiddlePointResponse;
import com.loadmapguide_backend.domain.location.entity.LocationPoint;
import com.loadmapguide_backend.global.common.enums.TransportationType;
import com.loadmapguide_backend.global.exception.BusinessException;
import com.loadmapguide_backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MiddlePointCalculator {
    
    private final LocationCoordinateService coordinateService;
    private final RouteCalculationService routeCalculationService;
    
    /**
     * 최적 중간지점 계산
     */
    public MiddlePointResponse calculateOptimalMeetingPoint(
            List<LocationRequest> startLocations,
            TransportationType transportationType) {
            
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("중간지점 계산 시작 - 참여자: {}, 교통수단: {}", 
                    startLocations.size(), transportationType);
            
            // 1. 좌표 정보 확보
            List<LocationPoint> coordinates = coordinateService.resolveCoordinates(startLocations);
            
            // 2. 기하학적 중심점 계산
            LocationPoint geometricCenter = calculateGeometricCenter(coordinates);
            
            // 3. 후보 지점들 생성
            List<OptimalLocation> candidates = generateCandidateLocations(
                    coordinates, geometricCenter, transportationType);
            
            // 4. 각 후보지점에 대한 이동시간 계산 및 점수화
            List<OptimalLocation> scoredCandidates = calculateScoresForCandidates(
                    candidates, coordinates, transportationType);
            
            // 5. 최적 지점 선정 (상위 5개)
            List<OptimalLocation> topCandidates = scoredCandidates.stream()
                    .sorted(Comparator.comparingDouble(OptimalLocation::getOverallScore).reversed())
                    .limit(5)
                    .collect(Collectors.toList());
            
            long calculationTime = System.currentTimeMillis() - startTime;
            
            log.info("중간지점 계산 완료 - 소요시간: {}ms, 후보지점: {}", 
                    calculationTime, topCandidates.size());
            
            return buildResponse(topCandidates, startLocations.size(), 
                               transportationType, calculationTime);
                               
        } catch (Exception e) {
            log.error("중간지점 계산 중 오류 발생", e);
            throw new BusinessException(ErrorCode.LOCATION_CALCULATION_FAILED, e);
        }
    }
    
    /**
     * 기하학적 중심점 계산
     */
    private LocationPoint calculateGeometricCenter(List<LocationPoint> coordinates) {
        if (coordinates.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_LOCATION);
        }
        
        double avgLat = coordinates.stream()
                .mapToDouble(LocationPoint::getLatitude)
                .average()
                .orElse(0.0);
                
        double avgLng = coordinates.stream()
                .mapToDouble(LocationPoint::getLongitude)
                .average()
                .orElse(0.0);
        
        // 중심점에 대해서도 역지오코딩 수행
        String centerAddress;
        String centerPlaceName;
        try {
            LocationPoint reverseGeocodedCenter = coordinateService.reverseGeocode(avgLat, avgLng);
            centerAddress = reverseGeocodedCenter.getAddress();
            centerPlaceName = reverseGeocodedCenter.getPlaceName();
        } catch (Exception e) {
            log.debug("중심점 역지오코딩 실패: ({}, {})", avgLat, avgLng);
            centerAddress = String.format("중심지점 (%.6f, %.6f)", avgLat, avgLng);
            centerPlaceName = "기하학적 중심점";
        }
        
        return LocationPoint.builder()
                .latitude(avgLat)
                .longitude(avgLng)
                .address(centerAddress)
                .placeName(centerPlaceName)
                .build();
    }
    
    /**
     * 후보 지점들 생성
     */
    private List<OptimalLocation> generateCandidateLocations(
            List<LocationPoint> startPoints, 
            LocationPoint center, 
            TransportationType transportationType) {
            
        List<OptimalLocation> candidates = new ArrayList<>();
        
        // 중심점을 기본 후보로 추가
        candidates.add(OptimalLocation.builder()
                .location(center)
                .build());
        
        // 중심점 주변 격자 패턴으로 후보지점 생성
        double[] offsets = {-0.005, -0.003, 0.0, 0.003, 0.005}; // 약 ±500m 간격
        
        for (double latOffset : offsets) {
            for (double lngOffset : offsets) {
                if (latOffset == 0.0 && lngOffset == 0.0) continue; // 중심점은 이미 추가됨
                
                double candidateLat = center.getLatitude() + latOffset;
                double candidateLng = center.getLongitude() + lngOffset;
                
                // 역지오코딩으로 실제 주소 조회
                String actualAddress;
                String placeName;
                try {
                    LocationPoint reverseGeocodedLocation = coordinateService.reverseGeocode(candidateLat, candidateLng);
                    actualAddress = reverseGeocodedLocation.getAddress();
                    placeName = reverseGeocodedLocation.getPlaceName();
                } catch (Exception e) {
                    log.debug("역지오코딩 실패, 좌표 표시: ({}, {})", candidateLat, candidateLng);
                    actualAddress = String.format("추천지점 (%.6f, %.6f)", candidateLat, candidateLng);
                    placeName = "중간지점";
                }
                
                LocationPoint candidate = LocationPoint.builder()
                        .latitude(candidateLat)
                        .longitude(candidateLng)
                        .address(actualAddress)
                        .placeName(placeName)
                        .build();
                        
                candidates.add(OptimalLocation.builder()
                        .location(candidate)
                        .build());
            }
        }
        
        return candidates;
    }
    
    /**
     * 후보지점들의 점수 계산
     */
    private List<OptimalLocation> calculateScoresForCandidates(
            List<OptimalLocation> candidates,
            List<LocationPoint> startPoints,
            TransportationType transportationType) {
            
        return candidates.stream()
                .map(candidate -> {
                    // 각 출발지에서의 이동시간 계산
                    double totalTravelTime = calculateTotalTravelTime(
                            startPoints, candidate.getLocation(), transportationType);
                    
                    // 상업지역 점수 계산 (간단한 휴리스틱)
                    double commercialScore = calculateCommercialScore(candidate.getLocation());
                    
                    // 전체 점수 계산
                    double overallScore = calculateOverallScore(totalTravelTime, commercialScore);
                    
                    return OptimalLocation.builder()
                            .location(candidate.getLocation())
                            .averageTravelTime(totalTravelTime / startPoints.size())
                            .commercialScore(commercialScore)
                            .overallScore(overallScore)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 총 이동시간 계산
     */
    private double calculateTotalTravelTime(
            List<LocationPoint> startPoints,
            LocationPoint destination,
            TransportationType transportationType) {
            
        return startPoints.stream()
                .mapToDouble(start -> {
                    // 실제로는 외부 API를 호출해야 하지만, 여기서는 직선거리 기반으로 근사치 계산
                    double distance = calculateDistance(start, destination);
                    
                    // 교통수단별 평균 속도로 시간 계산 (분 단위)
                    double speed = switch (transportationType) {
                        case PUBLIC_TRANSPORT -> 25.0; // km/h
                        case CAR -> 35.0;
                        case WALK -> 5.0;
                    };
                    
                    return (distance / 1000.0) / speed * 60; // 분 단위
                })
                .sum();
    }
    
    /**
     * 두 지점 간 거리 계산 (Haversine formula)
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
     * 상업지역 점수 계산 (개선된 휴리스틱)
     */
    private double calculateCommercialScore(LocationPoint location) {
        // 서울 주요 상권들의 좌표와 가중치
        Map<String, CommercialArea> majorAreas = Map.of(
            "강남역", new CommercialArea(37.4979, 127.0276, 100), // 최고 상업지역
            "홍대입구", new CommercialArea(37.5563, 126.9236, 95),
            "명동", new CommercialArea(37.5636, 126.9834, 90),
            "신촌", new CommercialArea(37.5559, 126.9364, 85),
            "건대입구", new CommercialArea(37.5403, 127.0695, 80),
            "이태원", new CommercialArea(37.5339, 126.9947, 75),
            "종로3가", new CommercialArea(37.5703, 126.9910, 70),
            "신림", new CommercialArea(37.4842, 126.9292, 65),
            "잠실", new CommercialArea(37.5133, 127.1028, 75),
            "구로디지털단지", new CommercialArea(37.4851, 126.8977, 60)
        );
        
        // 각 상권과의 거리 기반 점수 계산
        double maxScore = majorAreas.entrySet().stream()
                .mapToDouble(entry -> {
                    CommercialArea area = entry.getValue();
                    LocationPoint areaPoint = LocationPoint.builder()
                            .latitude(area.latitude)
                            .longitude(area.longitude)
                            .build();
                    
                    double distance = calculateDistance(location, areaPoint);
                    
                    // 거리별 점수 감소 (1km 이내 최대 점수, 5km 이후 최소 점수)
                    double distanceScore = Math.max(0, 1 - (distance / 5000)); // 5km에서 0점
                    return area.weight * distanceScore;
                })
                .max()
                .orElse(0.0);
        
        // 기본 점수 추가 (어느 지역이든 최소한의 편의시설은 있다고 가정)
        return Math.max(maxScore, 20.0) + getRegionBonus(location);
    }
    
    /**
     * 지역별 보너스 점수 (지하철역 접근성, 인구밀도 등을 고려한 추가 점수)
     */
    private double getRegionBonus(LocationPoint location) {
        // 서울 중심부 (한강 이북, 주요 구) 보너스
        if (location.getLatitude() > 37.52 && location.getLatitude() < 37.60) {
            if (location.getLongitude() > 126.95 && location.getLongitude() < 127.05) {
                return 10.0; // 중심부 보너스
            }
        }
        
        // 강남 지역 보너스
        if (location.getLatitude() > 37.47 && location.getLatitude() < 37.52) {
            if (location.getLongitude() > 127.02 && location.getLongitude() < 127.13) {
                return 15.0; // 강남 지역 보너스
            }
        }
        
        return 0.0;
    }
    
    // 상업지역 정보를 담는 내부 클래스
    private static class CommercialArea {
        final double latitude;
        final double longitude; 
        final double weight; // 상업지역 가중치 (0-100)
        
        CommercialArea(double latitude, double longitude, double weight) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.weight = weight;
        }
    }
    
    /**
     * 종합 점수 계산
     */
    private double calculateOverallScore(double travelTime, double commercialScore) {
        // 이동시간이 짧을수록 높은 점수 (60% 가중치)
        double timeScore = Math.max(0, 100 - travelTime * 2) * 0.6;
        
        // 상업지역 점수 (40% 가중치)  
        double commercialWeight = commercialScore * 0.4;
        
        return timeScore + commercialWeight;
    }
    
    /**
     * 응답 객체 생성
     */
    private MiddlePointResponse buildResponse(
            List<OptimalLocation> candidates,
            int participantCount,
            TransportationType transportationType,
            long calculationTime) {
            
        if (candidates.isEmpty()) {
            throw new BusinessException(ErrorCode.LOCATION_CALCULATION_FAILED);
        }
        
        OptimalLocation best = candidates.get(0);
        
        // 최적 지점
        MiddlePointResponse.LocationPoint optimalLocation = MiddlePointResponse.LocationPoint.builder()
                .latitude(best.getLocation().getLatitude())
                .longitude(best.getLocation().getLongitude())
                .address(best.getLocation().getAddress())
                .averageTravelTime(best.getAverageTravelTime())
                .commercialScore(best.getCommercialScore())
                .overallScore(best.getOverallScore())
                .build();
        
        // 후보 지점들
        List<MiddlePointResponse.LocationPoint> candidateLocations = candidates.stream()
                .map(candidate -> MiddlePointResponse.LocationPoint.builder()
                        .latitude(candidate.getLocation().getLatitude())
                        .longitude(candidate.getLocation().getLongitude())
                        .address(candidate.getLocation().getAddress())
                        .averageTravelTime(candidate.getAverageTravelTime())
                        .commercialScore(candidate.getCommercialScore())
                        .overallScore(candidate.getOverallScore())
                        .build())
                .collect(Collectors.toList());
        
        // 계산 정보
        MiddlePointResponse.CalculationInfo calculationInfo = MiddlePointResponse.CalculationInfo.builder()
                .totalParticipants(participantCount)
                .transportationType(transportationType.getDescription())
                .calculationTimeMs(calculationTime)
                .algorithm("기하학적 중심점 + 격자 탐색")
                .fairnessScore(calculateFairnessScore(best))
                .build();
        
        return MiddlePointResponse.builder()
                .optimalLocation(optimalLocation)
                .candidateLocations(candidateLocations)
                .calculationInfo(calculationInfo)
                .build();
    }
    
    private Double calculateFairnessScore(OptimalLocation location) {
        // 공평성 점수 계산 로직 (0-100점)
        // 실제로는 각 참여자의 이동시간 편차를 기반으로 계산
        return 85.0; // 임시값
    }
    
    /**
     * 최적화된 위치 정보를 담는 내부 클래스
     */
    @lombok.Builder
    @lombok.Getter
    private static class OptimalLocation {
        private LocationPoint location;
        private Double averageTravelTime;
        private Double commercialScore;
        private Double overallScore;
    }
}