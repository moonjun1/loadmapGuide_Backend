package com.loadmapguide_backend.domain.place.controller;

import com.loadmapguide_backend.domain.place.dto.PlaceResponse;
import com.loadmapguide_backend.domain.place.dto.PlaceSearchRequest;
import com.loadmapguide_backend.domain.place.service.PlaceSearchService;
import com.loadmapguide_backend.global.common.dto.BaseResponse;
import com.loadmapguide_backend.global.common.enums.PlaceCategory;
import com.loadmapguide_backend.global.common.enums.PlaceTag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

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
     * 태그 기반 장소 검색
     */
    @GetMapping("/search/tags")
    public BaseResponse<List<PlaceResponse>> searchPlacesByTags(
            @RequestParam Set<PlaceTag> tags,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(defaultValue = "2000") Integer radius,
            @RequestParam(defaultValue = "20") Integer limit) {
        
        log.info("태그 기반 장소 검색 - 태그: {}, 위치: ({}, {})", tags, latitude, longitude);
        
        List<PlaceResponse> places = placeSearchService.searchPlacesByTags(
                tags, latitude, longitude, radius, limit);
        
        return BaseResponse.success(
                String.format("태그 조건에 맞는 %d개의 장소를 찾았습니다.", places.size()),
                places);
    }
    
    /**
     * 카테고리와 태그 복합 검색
     */
    @GetMapping("/search/category-tags")
    public BaseResponse<List<PlaceResponse>> searchPlacesByCategoryAndTags(
            @RequestParam PlaceCategory category,
            @RequestParam Set<PlaceTag> tags,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(defaultValue = "2000") Integer radius,
            @RequestParam(defaultValue = "20") Integer limit) {
        
        log.info("카테고리-태그 복합 검색 - 카테고리: {}, 태그: {}", category, tags);
        
        List<PlaceResponse> places = placeSearchService.searchPlacesByCategoryAndTags(
                category, tags, latitude, longitude, radius, limit);
        
        return BaseResponse.success(
                String.format("%s 카테고리에서 태그 조건에 맞는 %d개의 장소를 찾았습니다.", 
                        category.getDescription(), places.size()),
                places);
    }
    
    /**
     * 모든 태그를 포함하는 장소 검색 (AND 조건)
     */
    @GetMapping("/search/all-tags")
    public BaseResponse<List<PlaceResponse>> searchPlacesByAllTags(
            @RequestParam Set<PlaceTag> tags,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(defaultValue = "2000") Integer radius,
            @RequestParam(defaultValue = "20") Integer limit) {
        
        log.info("전체 태그 포함 검색 - 태그: {}", tags);
        
        List<PlaceResponse> places = placeSearchService.searchPlacesByAllTags(
                tags, latitude, longitude, radius, limit);
        
        return BaseResponse.success(
                String.format("모든 태그를 포함하는 %d개의 장소를 찾았습니다.", places.size()),
                places);
    }
    
    /**
     * 사용 가능한 태그 목록 조회
     */
    @GetMapping("/tags")
    public BaseResponse<List<TagInfo>> getAvailableTags() {
        
        List<TagInfo> studyTags = List.of(
            new TagInfo(PlaceTag.QUIET_STUDY, "조용한 공부", "📚", "STUDY"),
            new TagInfo(PlaceTag.STUDY_CAFE, "스터디카페", "📖", "STUDY"),
            new TagInfo(PlaceTag.WIFI_GOOD, "와이파이 좋음", "📶", "STUDY"),
            new TagInfo(PlaceTag.POWER_OUTLET, "콘센트 많음", "🔌", "STUDY"),
            new TagInfo(PlaceTag.OPEN_24H, "24시간 운영", "🕐", "STUDY"),
            new TagInfo(PlaceTag.LIBRARY, "도서관/독서실", "📚", "STUDY")
        );
        
        List<TagInfo> foodTags = List.of(
            new TagInfo(PlaceTag.TASTY_FOOD, "맛집", "😋", "FOOD"),
            new TagInfo(PlaceTag.GOOD_VALUE, "가성비 좋음", "💰", "FOOD"),
            new TagInfo(PlaceTag.NICE_ATMOSPHERE, "분위기 좋음", "✨", "FOOD"),
            new TagInfo(PlaceTag.GROUP_FRIENDLY, "단체 가능", "👥", "FOOD"),
            new TagInfo(PlaceTag.RESERVATION_NEEDED, "예약 필수", "📞", "FOOD"),
            new TagInfo(PlaceTag.LATE_NIGHT, "야식/늦은 시간", "🌙", "FOOD")
        );
        
        List<TagInfo> entertainmentTags = List.of(
            new TagInfo(PlaceTag.KARAOKE, "노래방", "🎤", "ENTERTAINMENT"),
            new TagInfo(PlaceTag.BOWLING, "볼링장", "🎳", "ENTERTAINMENT"),
            new TagInfo(PlaceTag.CINEMA, "영화관", "🎬", "ENTERTAINMENT"),
            new TagInfo(PlaceTag.ARCADE, "오락실/게임", "🕹️", "ENTERTAINMENT"),
            new TagInfo(PlaceTag.BOARD_GAME, "보드게임", "🎲", "ENTERTAINMENT"),
            new TagInfo(PlaceTag.ESCAPE_ROOM, "방탈출", "🔐", "ENTERTAINMENT")
        );
        
        List<TagInfo> meetingTags = List.of(
            new TagInfo(PlaceTag.CONVERSATION, "대화하기 좋음", "💬", "MEETING"),
            new TagInfo(PlaceTag.SPACIOUS, "넓은 공간", "🏢", "MEETING"),
            new TagInfo(PlaceTag.PARKING, "주차 가능", "🅿️", "MEETING"),
            new TagInfo(PlaceTag.NICE_VIEW, "뷰 좋음", "🌆", "MEETING"),
            new TagInfo(PlaceTag.OUTDOOR, "야외 공간", "🌳", "MEETING")
        );
        
        List<TagInfo> accessibilityTags = List.of(
            new TagInfo(PlaceTag.SUBWAY_NEAR, "지하철 근처", "🚇", "ACCESSIBILITY"),
            new TagInfo(PlaceTag.BUS_NEAR, "버스 정류장 근처", "🚌", "ACCESSIBILITY"),
            new TagInfo(PlaceTag.WALK_ACCESSIBLE, "도보 접근 좋음", "🚶", "ACCESSIBILITY")
        );
        
        List<TagInfo> priceTags = List.of(
            new TagInfo(PlaceTag.BUDGET_FRIENDLY, "저렴함", "💸", "PRICE"),
            new TagInfo(PlaceTag.MID_RANGE, "적당한 가격", "💳", "PRICE"),
            new TagInfo(PlaceTag.PREMIUM, "고급스러움", "💎", "PRICE")
        );
        
        List<TagInfo> allTags = new java.util.ArrayList<>();
        allTags.addAll(studyTags);
        allTags.addAll(foodTags);
        allTags.addAll(entertainmentTags);
        allTags.addAll(meetingTags);
        allTags.addAll(accessibilityTags);
        allTags.addAll(priceTags);
        
        return BaseResponse.success("사용 가능한 태그 목록입니다.", allTags);
    }
    
    /**
     * 카테고리별 태그 목록 조회
     */
    @GetMapping("/tags/category/{category}")
    public BaseResponse<List<TagInfo>> getTagsByCategory(@PathVariable String category) {
        
        List<TagInfo> tags = switch (category.toUpperCase()) {
            case "STUDY" -> List.of(
                new TagInfo(PlaceTag.QUIET_STUDY, "조용한 공부", "📚", "STUDY"),
                new TagInfo(PlaceTag.STUDY_CAFE, "스터디카페", "📖", "STUDY"),
                new TagInfo(PlaceTag.WIFI_GOOD, "와이파이 좋음", "📶", "STUDY"),
                new TagInfo(PlaceTag.POWER_OUTLET, "콘센트 많음", "🔌", "STUDY"),
                new TagInfo(PlaceTag.OPEN_24H, "24시간 운영", "🕐", "STUDY"),
                new TagInfo(PlaceTag.LIBRARY, "도서관/독서실", "📚", "STUDY")
            );
            case "FOOD" -> List.of(
                new TagInfo(PlaceTag.TASTY_FOOD, "맛집", "😋", "FOOD"),
                new TagInfo(PlaceTag.GOOD_VALUE, "가성비 좋음", "💰", "FOOD"),
                new TagInfo(PlaceTag.NICE_ATMOSPHERE, "분위기 좋음", "✨", "FOOD"),
                new TagInfo(PlaceTag.GROUP_FRIENDLY, "단체 가능", "👥", "FOOD"),
                new TagInfo(PlaceTag.RESERVATION_NEEDED, "예약 필수", "📞", "FOOD"),
                new TagInfo(PlaceTag.LATE_NIGHT, "야식/늦은 시간", "🌙", "FOOD")
            );
            case "ENTERTAINMENT" -> List.of(
                new TagInfo(PlaceTag.KARAOKE, "노래방", "🎤", "ENTERTAINMENT"),
                new TagInfo(PlaceTag.BOWLING, "볼링장", "🎳", "ENTERTAINMENT"),
                new TagInfo(PlaceTag.CINEMA, "영화관", "🎬", "ENTERTAINMENT"),
                new TagInfo(PlaceTag.ARCADE, "오락실/게임", "🕹️", "ENTERTAINMENT"),
                new TagInfo(PlaceTag.BOARD_GAME, "보드게임", "🎲", "ENTERTAINMENT"),
                new TagInfo(PlaceTag.ESCAPE_ROOM, "방탈출", "🔐", "ENTERTAINMENT")
            );
            default -> List.of();
        };
        
        return BaseResponse.success(category + " 카테고리의 태그 목록입니다.", tags);
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
    
    /**
     * 태그 정보 DTO
     */
    public record TagInfo(PlaceTag tag, String displayName, String emoji, String category) {}
}