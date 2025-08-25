package com.loadmapguide_backend.domain.location.service;

import com.loadmapguide_backend.domain.location.dto.LocationRequest;
import com.loadmapguide_backend.domain.location.dto.RealTimeRouteResponse;
import com.loadmapguide_backend.global.common.enums.TransportationType;
import com.loadmapguide_backend.global.external.kakao.KakaoMapApiClient;
import com.loadmapguide_backend.global.external.kakao.dto.KakaoDirectionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealTimeTrafficService {
    
    private final KakaoMapApiClient kakaoMapApiClient;
    
    /**
     * 여러 출발지에서 목적지까지의 실시간 경로 정보 조회
     */
    @Cacheable(value = "realTimeRoutes", key = "'multi:' + #origins.hashCode() + ':' + #destLat + ':' + #destLng + ':' + #transportation")
    public List<RealTimeRouteResponse> getRealTimeRoutes(List<LocationRequest> origins, 
                                                        Double destLat, Double destLng, 
                                                        TransportationType transportation) {
        log.info("🚦 실시간 교통정보 조회 시작 - 목적지: ({}, {}), 교통수단: {}, 출발지 {}개", 
                destLat, destLng, transportation, origins.size());
        
        List<CompletableFuture<RealTimeRouteResponse>> futures = new ArrayList<>();
        
        // 비동기로 각 출발지에서의 경로 조회
        for (LocationRequest origin : origins) {
            CompletableFuture<RealTimeRouteResponse> future = CompletableFuture.supplyAsync(() -> {
                return calculateRealTimeRoute(origin, destLat, destLng, transportation);
            });
            futures.add(future);
        }
        
        // 모든 비동기 작업 완료 대기
        List<RealTimeRouteResponse> results = new ArrayList<>();
        for (CompletableFuture<RealTimeRouteResponse> future : futures) {
            try {
                results.add(future.get());
            } catch (Exception e) {
                log.error("❌ 실시간 경로 조회 중 오류 발생", e);
                // 오류가 발생한 경우 기본값으로 대체
                results.add(RealTimeRouteResponse.createDefault());
            }
        }
        
        log.info("✅ 실시간 교통정보 조회 완료 - {}개 경로 조회됨", results.size());
        return results;
    }
    
    /**
     * 단일 출발지에서 목적지까지의 실시간 경로 정보 계산
     */
    public RealTimeRouteResponse calculateRealTimeRoute(LocationRequest origin, 
                                                       Double destLat, Double destLng, 
                                                       TransportationType transportation) {
        try {
            log.debug("🛣️ 경로 계산: {} ({}, {}) -> ({}, {})", 
                    transportation, origin.getLatitude(), origin.getLongitude(), destLat, destLng);
            
            switch (transportation) {
                case CAR -> {
                    return calculateCarRoute(origin, destLat, destLng);
                }
                case PUBLIC_TRANSPORT -> {
                    return calculatePublicTransportRoute(origin, destLat, destLng);
                }
                case WALK -> {
                    return calculateWalkingRoute(origin, destLat, destLng);
                }
                default -> {
                    log.warn("⚠️ 지원하지 않는 교통수단: {}", transportation);
                    return RealTimeRouteResponse.createDefault();
                }
            }
            
        } catch (Exception e) {
            log.error("❌ 경로 계산 중 오류 발생: {} -> ({}, {})", origin.getAddress(), destLat, destLng, e);
            return RealTimeRouteResponse.createDefault();
        }
    }
    
    /**
     * 자동차 경로 계산 (카카오 Directions API 사용)
     */
    private RealTimeRouteResponse calculateCarRoute(LocationRequest origin, Double destLat, Double destLng) {
        try {
            // 실시간 교통정보가 반영된 경로 조회
            KakaoDirectionResponse response = kakaoMapApiClient.getCarRoute(
                    origin.getLongitude(), origin.getLatitude(),
                    destLng, destLat,
                    "RECOMMEND" // 추천 경로 (실시간 교통정보 반영)
            );
            
            if (response != null && response.isSuccess()) {
                return RealTimeRouteResponse.builder()
                        .originAddress(origin.getAddress())
                        .originLatitude(origin.getLatitude())
                        .originLongitude(origin.getLongitude())
                        .destLatitude(destLat)
                        .destLongitude(destLng)
                        .transportation(TransportationType.CAR)
                        .duration(response.getTotalDurationInMinutes())
                        .distance(response.getTotalDistance())
                        .trafficState(analyzeTrafficState(response))
                        .estimatedFare(response.getTaxiFare())
                        .tollFare(response.getTollFare())
                        .realTimeData(true)
                        .build();
            } else {
                log.warn("⚠️ 카카오 경로 API 응답 실패, 추정값 사용");
                return createEstimatedCarRoute(origin, destLat, destLng);
            }
            
        } catch (Exception e) {
            log.error("❌ 자동차 경로 조회 실패, 추정값 사용", e);
            return createEstimatedCarRoute(origin, destLat, destLng);
        }
    }
    
    /**
     * 대중교통 경로 계산 (추정값 기반)
     */
    private RealTimeRouteResponse calculatePublicTransportRoute(LocationRequest origin, Double destLat, Double destLng) {
        try {
            Integer estimatedTime = kakaoMapApiClient.estimatePublicTransportTime(
                    origin.getLongitude(), origin.getLatitude(),
                    destLng, destLat
            );
            
            // 직선거리 계산
            double distance = calculateDistance(origin.getLatitude(), origin.getLongitude(), destLat, destLng);
            
            return RealTimeRouteResponse.builder()
                    .originAddress(origin.getAddress())
                    .originLatitude(origin.getLatitude())
                    .originLongitude(origin.getLongitude())
                    .destLatitude(destLat)
                    .destLongitude(destLng)
                    .transportation(TransportationType.PUBLIC_TRANSPORT)
                    .duration(estimatedTime)
                    .distance((int) distance)
                    .trafficState("원활") // 대중교통은 일반적으로 안정적
                    .estimatedFare(estimatePublicTransportFare(distance))
                    .tollFare(0)
                    .realTimeData(false) // 추정값
                    .build();
                    
        } catch (Exception e) {
            log.error("❌ 대중교통 경로 계산 실패", e);
            return RealTimeRouteResponse.createDefault();
        }
    }
    
    /**
     * 도보 경로 계산
     */
    private RealTimeRouteResponse calculateWalkingRoute(LocationRequest origin, Double destLat, Double destLng) {
        try {
            Integer walkingTime = kakaoMapApiClient.calculateWalkingTime(
                    origin.getLongitude(), origin.getLatitude(),
                    destLng, destLat
            );
            
            double distance = calculateDistance(origin.getLatitude(), origin.getLongitude(), destLat, destLng);
            
            return RealTimeRouteResponse.builder()
                    .originAddress(origin.getAddress())
                    .originLatitude(origin.getLatitude())
                    .originLongitude(origin.getLongitude())
                    .destLatitude(destLat)
                    .destLongitude(destLng)
                    .transportation(TransportationType.WALK)
                    .duration(walkingTime)
                    .distance((int) distance)
                    .trafficState("해당없음")
                    .estimatedFare(0)
                    .tollFare(0)
                    .realTimeData(true) // 도보는 실시간 계산
                    .build();
                    
        } catch (Exception e) {
            log.error("❌ 도보 경로 계산 실패", e);
            return RealTimeRouteResponse.createDefault();
        }
    }
    
    /**
     * 자동차 경로 추정값 생성 (API 실패시 사용)
     */
    private RealTimeRouteResponse createEstimatedCarRoute(LocationRequest origin, Double destLat, Double destLng) {
        double distance = calculateDistance(origin.getLatitude(), origin.getLongitude(), destLat, destLng);
        // 평균 속도 40km/h로 가정
        int estimatedMinutes = (int) Math.max(5, (distance / 1000.0) * 1.5);
        
        return RealTimeRouteResponse.builder()
                .originAddress(origin.getAddress())
                .originLatitude(origin.getLatitude())
                .originLongitude(origin.getLongitude())
                .destLatitude(destLat)
                .destLongitude(destLng)
                .transportation(TransportationType.CAR)
                .duration(estimatedMinutes)
                .distance((int) distance)
                .trafficState("정보없음")
                .estimatedFare(estimateCarFare(distance))
                .tollFare(0)
                .realTimeData(false)
                .build();
    }
    
    /**
     * 교통 상황 분석
     */
    private String analyzeTrafficState(KakaoDirectionResponse response) {
        try {
            if (response.getFirstRoute() != null && response.getFirstRoute().getSections() != null) {
                // 도로별 교통상황 분석
                int totalRoads = 0;
                int congestionCount = 0;
                
                for (var section : response.getFirstRoute().getSections()) {
                    if (section.getRoads() != null) {
                        for (var road : section.getRoads()) {
                            totalRoads++;
                            if (road.getTrafficState() != null && road.getTrafficState() >= 3) {
                                congestionCount++;
                            }
                        }
                    }
                }
                
                if (totalRoads == 0) return "정보없음";
                
                double congestionRatio = (double) congestionCount / totalRoads;
                
                if (congestionRatio >= 0.6) return "정체";
                else if (congestionRatio >= 0.4) return "지체";
                else if (congestionRatio >= 0.2) return "서행";
                else return "원활";
            }
        } catch (Exception e) {
            log.warn("⚠️ 교통상황 분석 중 오류", e);
        }
        
        return "정보없음";
    }
    
    /**
     * 대중교통 요금 추정
     */
    private Integer estimatePublicTransportFare(double distanceInMeters) {
        // 서울 기준 대중교통 요금 (지하철 + 버스)
        if (distanceInMeters < 10000) return 1500; // 10km 미만
        else if (distanceInMeters < 20000) return 1800; // 20km 미만
        else return 2100; // 20km 이상
    }
    
    /**
     * 자동차 연료비 추정 (택시 요금 대신 연료비)
     */
    private Integer estimateCarFare(double distanceInMeters) {
        // 연비 10km/L, 휘발유 1600원/L 가정
        double fuelCost = (distanceInMeters / 1000.0) * 160;
        return (int) Math.max(1000, fuelCost);
    }
    
    /**
     * 두 좌표 간 직선거리 계산
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 지구 반지름 (km)
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
                
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c * 1000; // 미터로 변환
    }
    
    /**
     * 교통수단별 평균 이동시간 계산 (기존 MiddlePointCalculator와 연동)
     */
    public Double calculateAverageRealTimeTravelTime(List<LocationRequest> origins, 
                                                   Double destLat, Double destLng, 
                                                   TransportationType transportation) {
        try {
            List<RealTimeRouteResponse> routes = getRealTimeRoutes(origins, destLat, destLng, transportation);
            
            double totalTime = routes.stream()
                    .mapToDouble(route -> route.getDuration() != null ? route.getDuration() : 30.0)
                    .average()
                    .orElse(30.0);
            
            log.debug("📊 평균 실시간 이동시간: {}분 ({})", totalTime, transportation);
            
            return totalTime;
            
        } catch (Exception e) {
            log.error("❌ 평균 실시간 이동시간 계산 실패", e);
            return 30.0; // 기본값
        }
    }
}