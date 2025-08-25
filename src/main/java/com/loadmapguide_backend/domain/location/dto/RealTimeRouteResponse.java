package com.loadmapguide_backend.domain.location.dto;

import com.loadmapguide_backend.global.common.enums.TransportationType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RealTimeRouteResponse {
    
    private String originAddress;
    private Double originLatitude;
    private Double originLongitude;
    private Double destLatitude;
    private Double destLongitude;
    private TransportationType transportation;
    private Integer duration; // 분 단위
    private Integer distance; // 미터 단위
    private String trafficState; // 교통상황: "원활", "서행", "지체", "정체", "정보없음"
    private Integer estimatedFare; // 예상 요금 (원)
    private Integer tollFare; // 통행료 (원)
    private Boolean realTimeData; // 실시간 데이터 여부
    
    /**
     * 기본값으로 응답 생성 (오류 발생시 사용)
     */
    public static RealTimeRouteResponse createDefault() {
        return RealTimeRouteResponse.builder()
                .originAddress("알 수 없음")
                .originLatitude(0.0)
                .originLongitude(0.0)
                .destLatitude(0.0)
                .destLongitude(0.0)
                .transportation(TransportationType.PUBLIC_TRANSPORT)
                .duration(30)
                .distance(0)
                .trafficState("정보없음")
                .estimatedFare(0)
                .tollFare(0)
                .realTimeData(false)
                .build();
    }
    
    /**
     * 거리를 km 단위로 반환
     */
    public Double getDistanceInKm() {
        return distance != null ? distance / 1000.0 : 0.0;
    }
    
    /**
     * 시간을 "시간 분" 형태로 반환
     */
    public String getFormattedDuration() {
        if (duration == null || duration == 0) {
            return "정보없음";
        }
        
        if (duration >= 60) {
            int hours = duration / 60;
            int minutes = duration % 60;
            return minutes > 0 ? hours + "시간 " + minutes + "분" : hours + "시간";
        } else {
            return duration + "분";
        }
    }
    
    /**
     * 교통상황에 따른 색상 코드 반환 (Frontend에서 사용)
     */
    public String getTrafficStateColor() {
        return switch (trafficState) {
            case "원활" -> "#10B981"; // 초록색
            case "서행" -> "#F59E0B"; // 노란색
            case "지체" -> "#EF4444"; // 빨간색
            case "정체" -> "#DC2626"; // 진한 빨간색
            default -> "#6B7280";     // 회색
        };
    }
    
    /**
     * 교통수단 아이콘 반환
     */
    public String getTransportationIcon() {
        return switch (transportation) {
            case CAR -> "🚗";
            case SUBWAY -> "🚇";
            case BUS -> "🚌";
            case PUBLIC_TRANSPORT -> "🚊";
            case WALK -> "🚶";
        };
    }
    
    /**
     * 예상 요금을 원 단위로 포맷
     */
    public String getFormattedFare() {
        if (estimatedFare == null || estimatedFare == 0) {
            return "무료";
        }
        return String.format("%,d원", estimatedFare);
    }
    
    /**
     * 실시간 데이터 상태 표시
     */
    public String getRealTimeStatus() {
        return realTimeData ? "실시간" : "추정값";
    }
}