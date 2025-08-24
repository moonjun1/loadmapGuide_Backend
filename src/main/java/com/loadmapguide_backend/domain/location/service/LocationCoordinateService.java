package com.loadmapguide_backend.domain.location.service;

import com.loadmapguide_backend.domain.location.dto.LocationRequest;
import com.loadmapguide_backend.domain.location.entity.LocationPoint;
import com.loadmapguide_backend.domain.location.repository.LocationPointRepository;
import com.loadmapguide_backend.global.exception.BusinessException;
import com.loadmapguide_backend.global.exception.ErrorCode;
import com.loadmapguide_backend.global.external.kakao.KakaoMapApiClient;
import com.loadmapguide_backend.global.external.kakao.dto.KakaoCoordinateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationCoordinateService {
    
    private final LocationPointRepository locationPointRepository;
    private final KakaoMapApiClient kakaoMapApiClient;
    
    /**
     * 주소 목록을 좌표가 포함된 LocationPoint 목록으로 변환
     */
    public List<LocationPoint> resolveCoordinates(List<LocationRequest> locationRequests) {
        return locationRequests.stream()
                .map(this::resolveCoordinate)
                .collect(Collectors.toList());
    }
    
    /**
     * 단일 주소를 좌표가 포함된 LocationPoint로 변환
     */
    public LocationPoint resolveCoordinate(LocationRequest request) {
        try {
            // 1. 이미 좌표가 있는 경우
            if (request.hasCoordinates()) {
                return createOrUpdateLocationPoint(request);
            }
            
            // 2. DB에서 기존 좌표 조회
            Optional<LocationPoint> existingLocation = 
                    locationPointRepository.findByAddressAndLatitudeAndLongitude(
                            request.getAddress(), request.getLatitude(), request.getLongitude());
            
            if (existingLocation.isPresent()) {
                log.debug("기존 좌표 정보 사용: {}", request.getAddress());
                return existingLocation.get();
            }
            
            // 3. 좌표가 없는 경우 - 외부 API 호출 (현재는 임시 좌표)
            LocationPoint coordinates = geocodeAddress(request.getAddress());
            
            // 4. DB에 저장
            LocationPoint locationPoint = LocationPoint.builder()
                    .address(request.getAddress())
                    .latitude(coordinates.getLatitude())
                    .longitude(coordinates.getLongitude())
                    .placeName(request.getPlaceName())
                    .build();
            
            return locationPointRepository.save(locationPoint);
            
        } catch (Exception e) {
            log.error("좌표 변환 실패: {}", request.getAddress(), e);
            throw new BusinessException(ErrorCode.INVALID_LOCATION, e);
        }
    }
    
    /**
     * 기존 LocationPoint 생성 또는 업데이트
     */
    private LocationPoint createOrUpdateLocationPoint(LocationRequest request) {
        Optional<LocationPoint> existing = locationPointRepository
                .findByAddressAndLatitudeAndLongitude(
                        request.getAddress(), request.getLatitude(), request.getLongitude());
        
        if (existing.isPresent()) {
            return existing.get();
        }
        
        LocationPoint newLocation = LocationPoint.builder()
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .placeName(request.getPlaceName())
                .build();
        
        return locationPointRepository.save(newLocation);
    }
    
    /**
     * 주소를 좌표로 변환 (지오코딩)
     * 카카오맵 API를 사용하여 실제 좌표 변환
     * 주소 검색 실패시 키워드 검색 시도
     */
    private LocationPoint geocodeAddress(String address) {
        try {
            log.info("카카오맵 API로 주소 변환 시작: {}", address);
            
            // 주소 정규화
            String normalizedAddress = normalizeAddress(address);
            
            // 1. 먼저 주소 검색 API 시도
            KakaoCoordinateResponse response = kakaoMapApiClient.getCoordinateByAddress(normalizedAddress);
            
            // 주소 검색 성공시
            if (response != null && response.getDocuments() != null && !response.getDocuments().isEmpty()) {
                // 첫 번째 결과 사용 (정확도가 가장 높음)
                KakaoCoordinateResponse.Document document = response.getDocuments().get(0);
                
                LocationPoint result = LocationPoint.builder()
                        .latitude(Double.parseDouble(document.getLatitude()))
                        .longitude(Double.parseDouble(document.getLongitude()))
                        .address(document.getAddressName())
                        .placeName(document.getAddressName())
                        .build();
                        
                log.info("주소 변환 성공: {} -> ({}, {})", 
                        address, result.getLatitude(), result.getLongitude());
                        
                return result;
            }
            
            // 2. 주소 검색 실패시 키워드 검색 시도
            log.info("주소 검색 실패, 키워드 검색 시도: {}", address);
            return geocodeByKeyword(address);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("주소 변환 중 예외 발생: {}", address, e);
            
            // 키워드 검색도 시도해보기
            try {
                log.info("예외 발생으로 키워드 검색 시도: {}", address);
                return geocodeByKeyword(address);
            } catch (Exception keywordException) {
                log.error("키워드 검색도 실패: {}", address, keywordException);
                throw new BusinessException(ErrorCode.LOCATION_NOT_FOUND);
            }
        }
    }
    
    /**
     * 키워드로 장소 검색하여 좌표 변환
     */
    private LocationPoint geocodeByKeyword(String keyword) {
        try {
            log.info("키워드 검색 API로 장소 검색: {}", keyword);
            
            // 서울 중심부 좌표를 기준으로 검색 (서울시청)
            double centerLat = 37.5665;
            double centerLng = 126.9780;
            int radius = 20000; // 20km 반경
            
            var response = kakaoMapApiClient.searchPlacesByKeyword(keyword, centerLng, centerLat, radius);
            
            // 응답 검증
            if (response == null || response.getDocuments() == null || response.getDocuments().isEmpty()) {
                log.warn("키워드 검색에서도 결과를 찾을 수 없음: {}", keyword);
                throw new BusinessException(ErrorCode.LOCATION_NOT_FOUND);
            }
            
            // 첫 번째 결과 사용 (거리순 정렬)
            var document = response.getDocuments().get(0);
            
            LocationPoint result = LocationPoint.builder()
                    .latitude(Double.parseDouble(document.getLatitude()))
                    .longitude(Double.parseDouble(document.getLongitude()))
                    .address(document.getRoadAddressName() != null ? 
                            document.getRoadAddressName() : document.getAddressName())
                    .placeName(document.getPlaceName())
                    .build();
                    
            log.info("키워드 검색 성공: {} -> {} ({}, {})", 
                    keyword, document.getPlaceName(), result.getLatitude(), result.getLongitude());
                    
            return result;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("키워드 검색 중 예외 발생: {}", keyword, e);
            throw new BusinessException(ErrorCode.LOCATION_NOT_FOUND);
        }
    }
    
    /**
     * API 실패 시 사용할 폴백 좌표 생성
     */
    private LocationPoint createFallbackLocation(String address) {
        // 서울시청 좌표를 기본값으로 사용
        double fallbackLat = 37.5665;
        double fallbackLng = 126.9780;
        
        return LocationPoint.builder()
                .latitude(fallbackLat)
                .longitude(fallbackLng)
                .address(address)
                .build();
    }
    
    /**
     * 좌표를 주소로 변환 (역지오코딩)
     */
    public LocationPoint reverseGeocode(Double latitude, Double longitude) {
        try {
            log.info("카카오맵 API로 역지오코딩 시작: ({}, {})", latitude, longitude);
            
            // 좌표 유효성 검증
            if (!isValidCoordinates(latitude, longitude)) {
                throw new BusinessException(ErrorCode.INVALID_LOCATION);
            }
            
            // 카카오맵 API 호출
            KakaoCoordinateResponse response = kakaoMapApiClient.getAddressByCoordinate(longitude, latitude);
            
            // 응답 검증
            if (response == null || response.getDocuments() == null || response.getDocuments().isEmpty()) {
                log.warn("카카오맵 API에서 주소를 찾을 수 없음: ({}, {})", latitude, longitude);
                throw new BusinessException(ErrorCode.LOCATION_NOT_FOUND);
            }
            
            // 첫 번째 결과 사용
            KakaoCoordinateResponse.Document document = response.getDocuments().get(0);
            
            // 주소 정보 추출 (도로명 주소 우선, 없으면 지번 주소)
            String addressName = null;
            if (document.getRoadAddress() != null && document.getRoadAddress().getAddressName() != null) {
                addressName = document.getRoadAddress().getAddressName();
            } else if (document.getAddress() != null && document.getAddress().getAddressName() != null) {
                addressName = document.getAddress().getAddressName();
            } else if (document.getAddressName() != null) {
                addressName = document.getAddressName();
            }
            
            if (addressName == null) {
                log.warn("역지오코딩 결과에서 주소를 찾을 수 없음: ({}, {})", latitude, longitude);
                throw new BusinessException(ErrorCode.LOCATION_NOT_FOUND);
            }
            
            LocationPoint result = LocationPoint.builder()
                    .latitude(latitude)
                    .longitude(longitude)
                    .address(addressName)
                    .placeName(addressName) // 역지오코딩에서는 장소명이 주소와 동일
                    .build();
                    
            log.info("역지오코딩 성공: ({}, {}) -> {}", 
                    latitude, longitude, result.getAddress());
                    
            return result;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("역지오코딩 중 예외 발생: ({}, {})", latitude, longitude, e);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, e);
        }
    }
    
    /**
     * 좌표 유효성 검증
     */
    public boolean isValidCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return false;
        }
        
        // 대한민국 영역 대략적 검증
        boolean validLatitude = latitude >= 33.0 && latitude <= 39.0;
        boolean validLongitude = longitude >= 124.0 && longitude <= 132.0;
        
        return validLatitude && validLongitude;
    }
    
    /**
     * 주소 정규화 (추후 구현)
     */
    public String normalizeAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        
        return address.trim()
                .replaceAll("\\s+", " ")  // 연속된 공백 제거
                .replaceAll("[,，]", " "); // 쉼표를 공백으로 변환
    }
}