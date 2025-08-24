package com.loadmapguide_backend.domain.location.controller;

import com.loadmapguide_backend.domain.location.dto.LocationRequest;
import com.loadmapguide_backend.domain.location.dto.MeetingSessionRequest;
import com.loadmapguide_backend.domain.location.dto.MiddlePointResponse;
import com.loadmapguide_backend.domain.location.dto.MiddlePointResponseDto;
import com.loadmapguide_backend.domain.location.service.MiddlePointCalculator;
import com.loadmapguide_backend.global.common.dto.BaseResponse;
import com.loadmapguide_backend.global.external.weather.WeatherApiClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
public class LocationController {
    
    private final MiddlePointCalculator middlePointCalculator;
    private final WeatherApiClient weatherApiClient;
    
    /**
     * 중간지점 계산
     */
    @PostMapping("/middle-point")
    public BaseResponse<MiddlePointResponse> calculateMiddlePoint(
            @Valid @RequestBody MeetingSessionRequest request) {
            
        log.info("중간지점 계산 요청 - 세션: {}, 참여자: {}, 교통수단: {}", 
                request.getSessionName(), request.getParticipantCount(), request.getTransportationType());
        
        MiddlePointResponse response = middlePointCalculator.calculateOptimalMeetingPoint(
                request.getStartLocations(), request.getTransportationType());
        
        return BaseResponse.success("중간지점이 성공적으로 계산되었습니다.", response);
    }
    
    /**
     * 간단한 중간지점 계산 (세션 생성 없이)
     */
    @PostMapping("/middle-point/simple")
    public BaseResponse<MiddlePointResponse> calculateSimpleMiddlePoint(
            @Valid @RequestBody List<LocationRequest> locations,
            @RequestParam(defaultValue = "PUBLIC_TRANSPORT") String transportationType) {
            
        log.info("간단 중간지점 계산 요청 - 참여자: {}, 교통수단: {}", 
                locations.size(), transportationType);
        
        com.loadmapguide_backend.global.common.enums.TransportationType transport = 
                com.loadmapguide_backend.global.common.enums.TransportationType.valueOf(transportationType);
        
        MiddlePointResponse response = middlePointCalculator.calculateOptimalMeetingPoint(
                locations, transport);
        
        return BaseResponse.success("중간지점 계산이 완료되었습니다.", response);
    }
    
    /**
     * 날씨 정보가 포함된 중간지점 계산
     */
    @PostMapping("/middle-point/with-weather")
    public BaseResponse<MiddlePointResponseDto> calculateMiddlePointWithWeather(
            @Valid @RequestBody List<LocationRequest> locations,
            @RequestParam(defaultValue = "PUBLIC_TRANSPORT") String transportationType) {
            
        log.info("날씨 포함 중간지점 계산 요청 - 참여자: {}, 교통수단: {}", 
                locations.size(), transportationType);
        
        com.loadmapguide_backend.global.common.enums.TransportationType transport = 
                com.loadmapguide_backend.global.common.enums.TransportationType.valueOf(transportationType);
        
        // 중간지점 계산
        MiddlePointResponse response = middlePointCalculator.calculateOptimalMeetingPoint(
                locations, transport);
        
        // 최적 중간지점의 날씨 정보 조회
        MiddlePointResponse.LocationPoint bestCandidate = response.getOptimalLocation();
        var weatherResponse = weatherApiClient.getCurrentWeather(
                bestCandidate.getLatitude(), bestCandidate.getLongitude());
        
        // 응답 데이터 변환
        java.util.concurrent.atomic.AtomicInteger rank = new java.util.concurrent.atomic.AtomicInteger(1);
        List<MiddlePointResponseDto.CandidatePoint> candidates = response.getCandidateLocations().stream()
                .map(candidate -> new MiddlePointResponseDto.CandidatePoint(
                    rank.getAndIncrement(),
                    candidate.getLatitude(),
                    candidate.getLongitude(),
                    candidate.getOverallScore(),
                    candidate.getAddress() != null ? candidate.getAddress() : "주소 정보 없음",
                    candidate.getAverageTravelTime(),
                    candidate.getCommercialScore()
                )).toList();
        
        MiddlePointResponseDto.WeatherInfo weatherInfo = MiddlePointResponseDto.WeatherInfo.from(weatherResponse);
        
        MiddlePointResponseDto result = new MiddlePointResponseDto(
            "날씨 정보가 포함된 중간지점 계산이 완료되었습니다.",
            candidates,
            weatherInfo
        );
        
        return BaseResponse.success("중간지점 및 날씨 정보 조회가 완료되었습니다.", result);
    }

    /**
     * 좌표 유효성 검증
     */
    @PostMapping("/validate")
    public BaseResponse<Boolean> validateLocation(@Valid @RequestBody LocationRequest location) {
        
        log.debug("위치 유효성 검증 요청: {}", location.getAddress());
        
        boolean isValid = location.hasCoordinates() && 
                         isValidCoordinateRange(location.getLatitude(), location.getLongitude());
        
        return BaseResponse.success("위치 검증이 완료되었습니다.", isValid);
    }
    
    /**
     * 좌표 범위 유효성 검사
     */
    private boolean isValidCoordinateRange(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return false;
        }
        
        // 대한민국 영역 대략적 검증
        boolean validLatitude = latitude >= 33.0 && latitude <= 39.0;
        boolean validLongitude = longitude >= 124.0 && longitude <= 132.0;
        
        return validLatitude && validLongitude;
    }
}