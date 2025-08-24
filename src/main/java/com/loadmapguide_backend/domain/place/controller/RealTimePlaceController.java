package com.loadmapguide_backend.domain.place.controller;

import com.loadmapguide_backend.domain.place.entity.Place;
import com.loadmapguide_backend.domain.place.service.RealTimePlaceService;
import com.loadmapguide_backend.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/places/realtime")
@RequiredArgsConstructor
public class RealTimePlaceController {
    
    private final RealTimePlaceService realTimePlaceService;
    
    /**
     * 특정 위치 주변의 실시간 장소 정보 조회
     */
    @GetMapping("/around")
    public ResponseEntity<ApiResponse<List<Place>>> getRealTimePlacesAround(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "1000") Integer radius) {
        
        log.info("🔍 실시간 장소 조회 요청 - 위치: ({}, {}), 반경: {}m", latitude, longitude, radius);
        
        try {
            List<Place> places = realTimePlaceService.getRealTimePlacesAround(latitude, longitude, radius);
            
            return ResponseEntity.ok(
                ApiResponse.success(
                    places,
                    String.format("주변 %dm 내 %d개의 장소를 찾았습니다.", radius, places.size())
                )
            );
            
        } catch (Exception e) {
            log.error("❌ 실시간 장소 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("실시간 장소 정보 조회 중 오류가 발생했습니다.")
            );
        }
    }
    
    /**
     * 키워드 기반 실시간 장소 검색
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Place>>> searchRealTimePlaces(
            @RequestParam String keyword,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "2000") Integer radius) {
        
        log.info("🔍 키워드 검색 요청 - 키워드: {}, 위치: ({}, {})", keyword, latitude, longitude);
        
        try {
            List<Place> places = realTimePlaceService.searchRealTimePlaces(keyword, latitude, longitude, radius);
            
            return ResponseEntity.ok(
                ApiResponse.success(
                    places,
                    String.format("'%s' 검색 결과 %d개의 장소를 찾았습니다.", keyword, places.size())
                )
            );
            
        } catch (Exception e) {
            log.error("❌ 키워드 검색 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("장소 검색 중 오류가 발생했습니다.")
            );
        }
    }
    
    /**
     * 카테고리별 실시간 장소 검색
     */
    @GetMapping("/category/{categoryCode}")
    public ResponseEntity<ApiResponse<List<Place>>> searchPlacesByCategory(
            @PathVariable String categoryCode,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "1500") Integer radius) {
        
        log.info("🔍 카테고리 검색 요청 - 카테고리: {}, 위치: ({}, {})", categoryCode, latitude, longitude);
        
        try {
            List<Place> places = realTimePlaceService.searchRealTimePlacesByCategory(categoryCode, latitude, longitude, radius);
            
            return ResponseEntity.ok(
                ApiResponse.success(
                    places,
                    String.format("카테고리 '%s' 검색 결과 %d개의 장소를 찾았습니다.", categoryCode, places.size())
                )
            );
            
        } catch (Exception e) {
            log.error("❌ 카테고리 검색 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("카테고리별 장소 검색 중 오류가 발생했습니다.")
            );
        }
    }
    
    /**
     * 카페 전용 검색 (카공하기 좋은 카페 위주)
     */
    @GetMapping("/cafes")
    public ResponseEntity<ApiResponse<List<Place>>> searchCafesForWork(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "1000") Integer radius) {
        
        log.info("☕ 카공 카페 검색 요청 - 위치: ({}, {})", latitude, longitude);
        
        try {
            // 스터디카페와 일반 카페 모두 검색
            List<Place> studyCafes = realTimePlaceService.searchRealTimePlaces("스터디카페", latitude, longitude, radius);
            List<Place> cafes = realTimePlaceService.searchRealTimePlacesByCategory("CE7", latitude, longitude, radius);
            
            // 결합
            studyCafes.addAll(cafes);
            
            return ResponseEntity.ok(
                ApiResponse.success(
                    studyCafes,
                    String.format("카공하기 좋은 카페 %d개를 찾았습니다.", studyCafes.size())
                )
            );
            
        } catch (Exception e) {
            log.error("❌ 카페 검색 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("카페 검색 중 오류가 발생했습니다.")
            );
        }
    }
    
    /**
     * 맛집 검색 (모임하기 좋은 식당 위주)
     */
    @GetMapping("/restaurants")
    public ResponseEntity<ApiResponse<List<Place>>> searchRestaurantsForMeeting(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "1500") Integer radius) {
        
        log.info("🍽️ 모임 맛집 검색 요청 - 위치: ({}, {})", latitude, longitude);
        
        try {
            List<Place> restaurants = realTimePlaceService.searchRealTimePlacesByCategory("FD6", latitude, longitude, radius);
            
            return ResponseEntity.ok(
                ApiResponse.success(
                    restaurants,
                    String.format("모임하기 좋은 맛집 %d개를 찾았습니다.", restaurants.size())
                )
            );
            
        } catch (Exception e) {
            log.error("❌ 맛집 검색 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("맛집 검색 중 오류가 발생했습니다.")
            );
        }
    }
    
    /**
     * 놀거리/엔터테인먼트 검색
     */
    @GetMapping("/entertainment")
    public ResponseEntity<ApiResponse<List<Place>>> searchEntertainment(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "2000") Integer radius) {
        
        log.info("🎮 놀거리 검색 요청 - 위치: ({}, {})", latitude, longitude);
        
        try {
            // 다양한 엔터테인먼트 키워드로 검색
            List<Place> entertainment = realTimePlaceService.searchRealTimePlaces("오락실", latitude, longitude, radius);
            List<Place> karaoke = realTimePlaceService.searchRealTimePlaces("노래방", latitude, longitude, radius);
            List<Place> bowling = realTimePlaceService.searchRealTimePlaces("볼링장", latitude, longitude, radius);
            List<Place> cinema = realTimePlaceService.searchRealTimePlaces("영화관", latitude, longitude, radius);
            
            // 결합
            entertainment.addAll(karaoke);
            entertainment.addAll(bowling);
            entertainment.addAll(cinema);
            
            return ResponseEntity.ok(
                ApiResponse.success(
                    entertainment,
                    String.format("놀거리 %d개를 찾았습니다.", entertainment.size())
                )
            );
            
        } catch (Exception e) {
            log.error("❌ 놀거리 검색 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("놀거리 검색 중 오류가 발생했습니다.")
            );
        }
    }
}