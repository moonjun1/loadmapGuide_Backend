package com.loadmapguide_backend.domain.place.controller;

import com.loadmapguide_backend.domain.place.dto.PlaceResponse;
import com.loadmapguide_backend.domain.place.dto.PlaceSearchRequest;
import com.loadmapguide_backend.domain.place.service.PlaceSearchService;
import com.loadmapguide_backend.global.common.dto.BaseResponse;
import com.loadmapguide_backend.global.common.enums.PlaceCategory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/places")
@RequiredArgsConstructor
public class PlaceController {
    
    private final PlaceSearchService placeSearchService;
    
    /**
     * 주변 장소 검색
     */
    @PostMapping("/search")
    public BaseResponse<List<PlaceResponse>> searchPlaces(@Valid @RequestBody PlaceSearchRequest request) {
        
        log.info("장소 검색 요청 - 위치: ({}, {}), 반경: {}m, 카테고리: {}", 
                request.getLatitude(), request.getLongitude(), 
                request.getRadiusMeters(), request.getCategories());
        
        List<PlaceResponse> places = placeSearchService.searchNearbyPlaces(request);
        
        return BaseResponse.success(
                String.format("총 %d개의 장소를 찾았습니다.", places.size()), 
                places);
    }
    
    /**
     * 간단한 주변 장소 검색 (GET)
     */
    @GetMapping("/nearby")
    public BaseResponse<List<PlaceResponse>> searchNearbyPlaces(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "1000") Integer radius,
            @RequestParam(required = false) PlaceCategory category,
            @RequestParam(defaultValue = "20") Integer limit) {
        
        log.info("주변 장소 검색 - 위치: ({}, {}), 반경: {}m", latitude, longitude, radius);
        
        PlaceSearchRequest request = PlaceSearchRequest.builder()
                .latitude(latitude)
                .longitude(longitude)
                .radiusMeters(radius)
                .categories(category != null ? List.of(category) : null)
                .limit(limit)
                .sortBy("DISTANCE")
                .build();
        
        List<PlaceResponse> places = placeSearchService.searchNearbyPlaces(request);
        
        return BaseResponse.success("주변 장소 검색이 완료되었습니다.", places);
    }
    
    /**
     * 카테고리별 장소 검색
     */
    @GetMapping("/category/{category}")
    public BaseResponse<List<PlaceResponse>> searchPlacesByCategory(
            @PathVariable PlaceCategory category,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "2000") Integer radius) {
        
        log.info("카테고리별 장소 검색 - 카테고리: {}, 위치: ({}, {})", 
                category, latitude, longitude);
        
        List<PlaceResponse> places = placeSearchService.searchPlacesByCategory(
                latitude, longitude, radius, category);
        
        return BaseResponse.success(
                String.format("%s 카테고리 장소 %d개를 찾았습니다.", 
                        category.getDescription(), places.size()), 
                places);
    }
    
    /**
     * 장소 상세 정보 조회
     */
    @GetMapping("/{placeId}")
    public BaseResponse<PlaceResponse> getPlaceDetail(@PathVariable Long placeId) {
        
        log.info("장소 상세 정보 조회 - ID: {}", placeId);
        
        PlaceResponse place = placeSearchService.getPlaceDetail(placeId);
        
        return BaseResponse.success("장소 정보를 조회했습니다.", place);
    }
    
    /**
     * 카카오 장소 ID로 조회
     */
    @GetMapping("/kakao/{kakaoPlaceId}")
    public BaseResponse<PlaceResponse> getPlaceByKakaoId(@PathVariable String kakaoPlaceId) {
        
        log.info("카카오 장소 ID로 조회 - ID: {}", kakaoPlaceId);
        
        PlaceResponse place = placeSearchService.getPlaceByKakaoId(kakaoPlaceId);
        
        return BaseResponse.success("장소 정보를 조회했습니다.", place);
    }
    
    /**
     * 사용 가능한 카테고리 목록 조회
     */
    @GetMapping("/categories")
    public BaseResponse<List<CategoryInfo>> getAvailableCategories() {
        
        List<CategoryInfo> categories = List.of(
            new CategoryInfo(PlaceCategory.CAFE, "카페", "CE7"),
            new CategoryInfo(PlaceCategory.RESTAURANT, "음식점", "FD6"),
            new CategoryInfo(PlaceCategory.STUDY_CAFE, "스터디카페", "STUDY"),
            new CategoryInfo(PlaceCategory.ENTERTAINMENT, "문화시설", "CT1"),
            new CategoryInfo(PlaceCategory.PARK, "공원", "AT4"),
            new CategoryInfo(PlaceCategory.SHOPPING, "쇼핑", "MT1")
        );
        
        return BaseResponse.success("사용 가능한 카테고리 목록입니다.", categories);
    }
    
    /**
     * 카테고리 정보 DTO
     */
    public record CategoryInfo(PlaceCategory category, String description, String code) {}
}