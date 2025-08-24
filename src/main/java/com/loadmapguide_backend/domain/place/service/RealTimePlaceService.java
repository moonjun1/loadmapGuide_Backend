package com.loadmapguide_backend.domain.place.service;

import com.loadmapguide_backend.domain.place.entity.Place;
import com.loadmapguide_backend.domain.place.entity.BusinessHours;
import com.loadmapguide_backend.domain.place.entity.PlaceAdditionalInfo;
import com.loadmapguide_backend.domain.place.repository.PlaceRepository;
import com.loadmapguide_backend.global.common.enums.PlaceCategory;
import com.loadmapguide_backend.global.external.kakao.KakaoMapApiClient;
import com.loadmapguide_backend.global.external.kakao.dto.KakaoPlaceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RealTimePlaceService {
    
    private final KakaoMapApiClient kakaoMapApiClient;
    private final PlaceRepository placeRepository;
    
    /**
     * 특정 위치 주변의 실시간 장소 정보 조회
     */
    @Cacheable(value = "realTimePlaces", key = "'location:' + #latitude + ':' + #longitude + ':' + #radius")
    public List<Place> getRealTimePlacesAround(Double latitude, Double longitude, Integer radius) {
        log.info("🔍 실시간 장소 정보 조회 시작 - 위치: ({}, {}), 반경: {}m", latitude, longitude, radius);
        
        List<Place> realTimePlaces = new ArrayList<>();
        
        try {
            // 카페 검색
            List<Place> cafes = searchAndConvertPlaces("카페", latitude, longitude, radius, "CE7", PlaceCategory.CAFE);
            realTimePlaces.addAll(cafes);
            
            // 식당 검색
            List<Place> restaurants = searchAndConvertPlaces("식당", latitude, longitude, radius, "FD6", PlaceCategory.RESTAURANT);
            realTimePlaces.addAll(restaurants);
            
            // 스터디카페 검색
            List<Place> studyCafes = searchAndConvertPlaces("스터디카페", latitude, longitude, radius, "CE7", PlaceCategory.STUDY_CAFE);
            realTimePlaces.addAll(studyCafes);
            
            // 쇼핑 검색
            List<Place> shopping = searchAndConvertPlaces("쇼핑몰", latitude, longitude, radius, "MT1", PlaceCategory.SHOPPING);
            realTimePlaces.addAll(shopping);
            
            // 공원 검색
            List<Place> parks = searchAndConvertPlaces("공원", latitude, longitude, radius, "AT4", PlaceCategory.PARK);
            realTimePlaces.addAll(parks);
            
            log.info("✅ 실시간 장소 정보 조회 완료 - 총 {}개 장소 발견", realTimePlaces.size());
            
        } catch (Exception e) {
            log.error("❌ 실시간 장소 정보 조회 중 오류 발생", e);
        }
        
        return realTimePlaces;
    }
    
    /**
     * 키워드 기반 실시간 장소 검색
     */
    @Cacheable(value = "realTimePlaces", key = "'keyword:' + #keyword + ':' + #latitude + ':' + #longitude + ':' + #radius")
    public List<Place> searchRealTimePlaces(String keyword, Double latitude, Double longitude, Integer radius) {
        log.info("🔍 키워드 기반 실시간 장소 검색 - 키워드: {}, 위치: ({}, {})", keyword, latitude, longitude);
        
        try {
            return searchAndConvertPlaces(keyword, latitude, longitude, radius, null, categorizeByKeyword(keyword));
        } catch (Exception e) {
            log.error("❌ 키워드 기반 장소 검색 중 오류 발생", e);
            return List.of();
        }
    }
    
    /**
     * 카테고리별 실시간 장소 검색
     */
    @Cacheable(value = "realTimePlaces", key = "'category:' + #category + ':' + #latitude + ':' + #longitude + ':' + #radius")
    public List<Place> searchRealTimePlacesByCategory(String categoryCode, Double latitude, Double longitude, Integer radius) {
        log.info("🔍 카테고리별 실시간 장소 검색 - 카테고리: {}, 위치: ({}, {})", categoryCode, latitude, longitude);
        
        try {
            KakaoPlaceResponse response = kakaoMapApiClient.searchPlacesByCategory(categoryCode, longitude, latitude, radius);
            
            if (!response.hasResults()) {
                log.info("ℹ️ 해당 카테고리의 장소가 없습니다: {}", categoryCode);
                return List.of();
            }
            
            return convertToPlaceList(response, categorizeByCategoryCode(categoryCode));
            
        } catch (Exception e) {
            log.error("❌ 카테고리별 장소 검색 중 오류 발생", e);
            return List.of();
        }
    }
    
    /**
     * 장소 검색 및 변환 통합 메서드
     */
    private List<Place> searchAndConvertPlaces(String keyword, Double latitude, Double longitude, Integer radius, String categoryCode, PlaceCategory category) {
        try {
            KakaoPlaceResponse response;
            
            if (categoryCode != null) {
                // 카테고리 검색과 키워드 검색을 병행
                response = kakaoMapApiClient.searchPlacesByCategory(categoryCode, longitude, latitude, radius);
                
                // 카테고리 검색 결과가 적을 경우 키워드 검색 추가
                if (!response.hasResults() || response.getResults().size() < 5) {
                    KakaoPlaceResponse keywordResponse = kakaoMapApiClient.searchPlacesByKeyword(keyword, longitude, latitude, radius);
                    if (keywordResponse.hasResults()) {
                        response = keywordResponse;
                    }
                }
            } else {
                response = kakaoMapApiClient.searchPlacesByKeyword(keyword, longitude, latitude, radius);
            }
            
            if (!response.hasResults()) {
                log.info("ℹ️ 해당 조건의 장소가 없습니다: {}", keyword);
                return List.of();
            }
            
            return convertToPlaceList(response, category);
            
        } catch (Exception e) {
            log.error("❌ 장소 검색 중 오류 발생: {}", keyword, e);
            return List.of();
        }
    }
    
    /**
     * 카카오 API 응답을 Place 엔티티로 변환
     */
    private List<Place> convertToPlaceList(KakaoPlaceResponse response, PlaceCategory category) {
        List<Place> places = new ArrayList<>();
        
        for (KakaoPlaceResponse.Document doc : response.getResults()) {
            try {
                Place place = Place.builder()
                        .kakaoPlaceId(doc.getId())
                        .name(doc.getPlaceName())
                        .address(doc.getAddressName())
                        .roadAddress(doc.getRoadAddressName())
                        .phone(doc.getPhone())
                        .latitude(parseDouble(doc.getLatitude()))
                        .longitude(parseDouble(doc.getLongitude()))
                        .placeUrl(doc.getPlaceUrl())
                        .category(category)
                        .rating(generateEstimatedRating()) // 실제 평점이 없으므로 추정값
                        .priceRange(estimatePriceRange(category))
                        .businessHours(generateEstimatedBusinessHours(category))
                        .additionalInfo(generateAdditionalInfo(doc))
                        .build();
                
                places.add(place);
                
            } catch (Exception e) {
                log.warn("⚠️ 장소 변환 중 오류 발생: {} - {}", doc.getPlaceName(), e.getMessage());
            }
        }
        
        log.info("✅ {}개 장소 변환 완료", places.size());
        return places;
    }
    
    /**
     * 키워드로 카테고리 추정
     */
    private PlaceCategory categorizeByKeyword(String keyword) {
        if (keyword.contains("카페") || keyword.contains("커피")) {
            return PlaceCategory.CAFE;
        } else if (keyword.contains("식당") || keyword.contains("맛집") || keyword.contains("음식")) {
            return PlaceCategory.RESTAURANT;
        } else if (keyword.contains("스터디") || keyword.contains("독서실")) {
            return PlaceCategory.STUDY_CAFE;
        } else if (keyword.contains("쇼핑") || keyword.contains("몰") || keyword.contains("마트")) {
            return PlaceCategory.SHOPPING;
        } else if (keyword.contains("공원") || keyword.contains("산책")) {
            return PlaceCategory.PARK;
        } else {
            return PlaceCategory.ENTERTAINMENT;
        }
    }
    
    /**
     * 카테고리 코드로 카테고리 추정
     */
    private PlaceCategory categorizeByCategoryCode(String categoryCode) {
        return switch (categoryCode) {
            case "CE7" -> PlaceCategory.CAFE;
            case "FD6" -> PlaceCategory.RESTAURANT;
            case "MT1", "CS2" -> PlaceCategory.SHOPPING;
            case "AT4" -> PlaceCategory.PARK;
            default -> PlaceCategory.ENTERTAINMENT;
        };
    }
    
    /**
     * 추정 평점 생성 (실제 평점 데이터가 없으므로)
     */
    private Double generateEstimatedRating() {
        // 3.5 ~ 4.5 사이의 랜덤한 평점
        return 3.5 + Math.random() * 1.0;
    }
    
    /**
     * 카테고리별 추정 가격대
     */
    private Integer estimatePriceRange(PlaceCategory category) {
        return switch (category) {
            case CAFE -> 2; // 중간
            case RESTAURANT -> 3; // 보통
            case STUDY_CAFE -> 2; // 중간
            case SHOPPING -> 3; // 보통
            case PARK -> 1; // 저렴
            case ENTERTAINMENT -> 3; // 보통
        };
    }
    
    /**
     * 카테고리별 추정 영업시간
     */
    private BusinessHours generateEstimatedBusinessHours(PlaceCategory category) {
        return switch (category) {
            case CAFE -> BusinessHours.builder()
                .holidayInfo("연중무휴")
                .specialNote("주말 영업시간 연장")
                .build();
            case RESTAURANT -> BusinessHours.builder()
                .holidayInfo("월요일 휴무")
                .specialNote("브레이크타임 15:00-17:00")
                .build();
            case STUDY_CAFE -> BusinessHours.builder()
                .holidayInfo("연중무휴")
                .specialNote("24시간 운영 (일요일 23시 마감)")
                .build();
            case SHOPPING -> BusinessHours.builder()
                .holidayInfo("둘째, 넷째 월요일 휴무")
                .specialNote("주차 2시간 무료")
                .build();
            case PARK -> BusinessHours.builder()
                .holidayInfo("연중무휴")
                .specialNote("야간 조명 22:00까지")
                .build();
            default -> BusinessHours.builder()
                .holidayInfo("월요일 휴무")
                .specialNote("일반 운영시간")
                .build();
        };
    }
    
    /**
     * 추가 정보 생성
     */
    private PlaceAdditionalInfo generateAdditionalInfo(KakaoPlaceResponse.Document doc) {
        return PlaceAdditionalInfo.builder()
                .wifiAvailable(true) // 대부분의 현대 시설에서 제공
                .cardPayment(true)   // 대부분 카드 결제 가능
                .parkingInfo(doc.getCategoryGroupCode() != null && 
                           doc.getCategoryGroupCode().equals("MT1") ? "주차 가능" : "주차 정보 없음")
                .atmosphere("카카오맵 등록 장소")
                .reviewCount((int) (Math.random() * 100) + 10) // 10-110 사이 랜덤
                .build();
    }
    
    /**
     * 문자열을 Double로 안전하게 변환
     */
    private Double parseDouble(String str) {
        try {
            return str != null ? Double.parseDouble(str) : 0.0;
        } catch (NumberFormatException e) {
            log.warn("⚠️ 숫자 변환 실패: {}", str);
            return 0.0;
        }
    }
    
    /**
     * 장소 정보를 데이터베이스에 저장 또는 업데이트
     */
    @Transactional
    public Place saveOrUpdatePlace(Place place) {
        try {
            // 카카오 Place ID로 기존 장소 찾기
            return placeRepository.findByKakaoPlaceId(place.getKakaoPlaceId())
                    .map(existingPlace -> {
                        // 기존 장소가 있다면 업데이트
                        existingPlace.updateRealTimeInfo(
                                place.getName(),
                                place.getPhone(),
                                place.getRating(),
                                place.getBusinessHours(),
                                place.getAdditionalInfo()
                        );
                        log.debug("📝 기존 장소 정보 업데이트: {}", existingPlace.getName());
                        return placeRepository.save(existingPlace);
                    })
                    .orElseGet(() -> {
                        // 새로운 장소라면 저장
                        log.debug("🆕 새로운 장소 저장: {}", place.getName());
                        return placeRepository.save(place);
                    });
        } catch (Exception e) {
            log.error("❌ 장소 저장/업데이트 중 오류 발생: {}", place.getName(), e);
            throw e;
        }
    }
}