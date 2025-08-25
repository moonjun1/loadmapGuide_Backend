package com.loadmapguide_backend.domain.place.service;

import com.loadmapguide_backend.domain.place.dto.PlaceResponse;
import com.loadmapguide_backend.domain.place.dto.PlaceSearchRequest;
import com.loadmapguide_backend.domain.place.entity.Place;
import com.loadmapguide_backend.domain.place.repository.PlaceRepository;
import com.loadmapguide_backend.global.common.enums.PlaceCategory;
import com.loadmapguide_backend.global.common.enums.PlaceTag;
import com.loadmapguide_backend.global.exception.BusinessException;
import com.loadmapguide_backend.global.exception.ErrorCode;
import com.loadmapguide_backend.global.external.kakao.KakaoMapApiClient;
import com.loadmapguide_backend.global.external.kakao.dto.KakaoPlaceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceSearchService {
    
    private final PlaceRepository placeRepository;
    private final KakaoMapApiClient kakaoMapApiClient;
    
    /**
     * 주변 장소 검색 (DB + 카카오 API 통합)
     */
    public List<PlaceResponse> searchNearbyPlaces(PlaceSearchRequest request) {
        try {
            log.info("장소 검색 시작 - 위치: ({}, {}), 반경: {}m", 
                    request.getLatitude(), request.getLongitude(), request.getRadiusMeters());
            
            // 1. DB에서 기존 장소 검색
            List<Place> dbPlaces = searchPlacesByLocation(request);
            log.debug("DB에서 찾은 장소: {}개", dbPlaces.size());
            
            // 2. 카카오 API에서 장소 검색
            List<Place> kakaoPlaces = searchPlacesFromKakaoApi(request);
            log.debug("카카오 API에서 찾은 장소: {}개", kakaoPlaces.size());
            
            // 3. 두 결과 병합 (중복 제거)
            List<Place> combinedPlaces = Stream.concat(dbPlaces.stream(), kakaoPlaces.stream())
                    .distinct() // Place 엔티티에 equals/hashcode 구현 필요
                    .collect(Collectors.toList());
            
            // 4. 필터링 적용
            List<Place> filteredPlaces = applyFilters(combinedPlaces, request);
            
            // 5. 정렬 및 제한
            List<Place> sortedPlaces = applySortingAndLimit(filteredPlaces, request);
            
            // 6. 응답 변환
            List<PlaceResponse> responses = sortedPlaces.stream()
                    .map(place -> {
                        double distance = place.calculateDistance(request.getLatitude(), request.getLongitude());
                        return PlaceResponse.from(place, distance);
                    })
                    .collect(Collectors.toList());
            
            log.info("장소 검색 완료 - 총 {}개 장소 발견 (DB: {}개, API: {}개)", 
                    responses.size(), dbPlaces.size(), kakaoPlaces.size());
            return responses;
            
        } catch (Exception e) {
            log.error("장소 검색 중 오류 발생", e);
            throw new BusinessException(ErrorCode.PLACE_SEARCH_FAILED, e);
        }
    }
    
    /**
     * 카카오 API에서 장소 검색
     */
    private List<Place> searchPlacesFromKakaoApi(PlaceSearchRequest request) {
        try {
            List<Place> apiResults = new ArrayList<>();
            
            // 키워드가 있으면 키워드 검색
            if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                KakaoPlaceResponse keywordResponse = kakaoMapApiClient.searchPlacesByKeyword(
                        request.getKeyword(), 
                        request.getLongitude(), 
                        request.getLatitude(), 
                        request.getRadiusMeters()
                );
                apiResults.addAll(convertKakaoResponseToPlaces(keywordResponse));
            }
            
            // 카테고리별 검색
            if (request.getCategories() != null && !request.getCategories().isEmpty()) {
                for (PlaceCategory category : request.getCategories()) {
                    String categoryCode = getCategoryCode(category);
                    if (categoryCode != null) {
                        KakaoPlaceResponse categoryResponse = kakaoMapApiClient.searchPlacesByCategory(
                                categoryCode, 
                                request.getLongitude(), 
                                request.getLatitude(), 
                                request.getRadiusMeters()
                        );
                        apiResults.addAll(convertKakaoResponseToPlaces(categoryResponse));
                    }
                }
            }
            
            // 카테고리 지정이 없고 키워드도 없으면 주요 카테고리들로 검색
            if ((request.getCategories() == null || request.getCategories().isEmpty()) && 
                (request.getKeyword() == null || request.getKeyword().trim().isEmpty())) {
                
                String[] defaultCategories = {"CE7", "FD6", "MT1"}; // 카페, 음식점, 마트
                for (String categoryCode : defaultCategories) {
                    KakaoPlaceResponse response = kakaoMapApiClient.searchPlacesByCategory(
                            categoryCode, 
                            request.getLongitude(), 
                            request.getLatitude(), 
                            request.getRadiusMeters()
                    );
                    apiResults.addAll(convertKakaoResponseToPlaces(response));
                }
            }
            
            return apiResults.stream()
                    .distinct()
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.warn("카카오 API 검색 실패, DB 결과만 사용: {}", e.getMessage());
            return new ArrayList<>(); // API 실패 시 빈 리스트 반환
        }
    }
    
    /**
     * 카테고리별 장소 검색
     */
    public List<PlaceResponse> searchPlacesByCategory(Double latitude, Double longitude, 
                                                    Integer radiusMeters, PlaceCategory category) {
        
        PlaceSearchRequest request = PlaceSearchRequest.builder()
                .latitude(latitude)
                .longitude(longitude)
                .radiusMeters(radiusMeters)
                .categories(List.of(category))
                .build();
        
        return searchNearbyPlaces(request);
    }
    
    /**
     * 장소 상세 정보 조회
     */
    public PlaceResponse getPlaceDetail(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));
        
        return PlaceResponse.from(place);
    }
    
    /**
     * 카카오 장소 ID로 조회
     */
    public PlaceResponse getPlaceByKakaoId(String kakaoPlaceId) {
        Place place = placeRepository.findByKakaoPlaceId(kakaoPlaceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PLACE_NOT_FOUND));
        
        return PlaceResponse.from(place);
    }
    
    /**
     * 위치 기반 장소 검색
     */
    private List<Place> searchPlacesByLocation(PlaceSearchRequest request) {
        // 검색 영역 계산 (위도/경도 범위)
        double latRange = request.getRadiusMeters() / 111000.0; // 1도 = 약 111km
        double lngRange = request.getRadiusMeters() / (111000.0 * Math.cos(Math.toRadians(request.getLatitude())));
        
        double minLat = request.getLatitude() - latRange;
        double maxLat = request.getLatitude() + latRange;
        double minLng = request.getLongitude() - lngRange;
        double maxLng = request.getLongitude() + lngRange;
        
        if (request.getCategories() != null && !request.getCategories().isEmpty()) {
            // 카테고리별 검색
            return request.getCategories().stream()
                    .flatMap(category -> 
                            placeRepository.findPlacesByCategoryInArea(category, minLat, maxLat, minLng, maxLng).stream())
                    .distinct()
                    .collect(Collectors.toList());
        } else {
            // 전체 카테고리 검색
            return placeRepository.findPlacesInArea(minLat, maxLat, minLng, maxLng);
        }
    }
    
    /**
     * 필터 조건 적용
     */
    private List<Place> applyFilters(List<Place> places, PlaceSearchRequest request) {
        return places.stream()
                .filter(place -> {
                    // 거리 필터
                    double distance = place.calculateDistance(request.getLatitude(), request.getLongitude());
                    if (distance > request.getRadiusMeters()) {
                        return false;
                    }
                    
                    // 예산 필터
                    if (request.getMaxBudget() != null && place.getPriceRange() != null) {
                        if (place.getPriceRange() > request.getMaxBudget()) {
                            return false;
                        }
                    }
                    
                    // 평점 필터
                    if (request.getMinRating() != null && place.getRating() != null) {
                        if (place.getRating() < request.getMinRating()) {
                            return false;
                        }
                    }
                    
                    // 키워드 필터
                    if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                        String keyword = request.getKeyword().toLowerCase();
                        String placeName = place.getName() != null ? place.getName().toLowerCase() : "";
                        String placeAddress = place.getAddress() != null ? place.getAddress().toLowerCase() : "";
                        
                        if (!placeName.contains(keyword) && !placeAddress.contains(keyword)) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 정렬 및 개수 제한 적용
     */
    private List<Place> applySortingAndLimit(List<Place> places, PlaceSearchRequest request) {
        Comparator<Place> comparator = switch (request.getSortBy().toUpperCase()) {
            case "RATING" -> Comparator.comparing(Place::getRating, 
                    Comparator.nullsLast(Comparator.reverseOrder()));
            case "POPULARITY" -> Comparator.comparing(place -> {
                if (place.getAdditionalInfo() != null && place.getAdditionalInfo().getReviewCount() != null) {
                    return place.getAdditionalInfo().getReviewCount();
                }
                return 0;
            }, Comparator.reverseOrder());
            default -> Comparator.comparing(place -> 
                    place.calculateDistance(request.getLatitude(), request.getLongitude()));
        };
        
        return places.stream()
                .sorted(comparator)
                .limit(request.getLimit())
                .collect(Collectors.toList());
    }
    
    /**
     * 태그 기반 장소 검색
     */
    public List<PlaceResponse> searchPlacesByTags(Set<PlaceTag> tags, Double latitude, Double longitude, 
                                                 Integer radiusMeters, Integer limit) {
        try {
            log.info("태그 기반 장소 검색 시작 - 태그: {}, 위치: ({}, {})", tags, latitude, longitude);
            
            List<Place> places;
            
            if (latitude != null && longitude != null && radiusMeters != null) {
                // 위치 기반 태그 검색
                double latRange = radiusMeters / 111000.0;
                double lngRange = radiusMeters / (111000.0 * Math.cos(Math.toRadians(latitude)));
                
                double minLat = latitude - latRange;
                double maxLat = latitude + latRange;
                double minLng = longitude - lngRange;
                double maxLng = longitude + lngRange;
                
                places = placeRepository.findByTagsInArea(tags, minLat, maxLat, minLng, maxLng);
            } else {
                // 전체 영역에서 태그 검색
                places = placeRepository.findByTagsIn(tags);
            }
            
            // 거리 계산 및 정렬
            if (latitude != null && longitude != null) {
                places = places.stream()
                        .filter(place -> {
                            if (radiusMeters != null) {
                                double distance = place.calculateDistance(latitude, longitude);
                                return distance <= radiusMeters;
                            }
                            return true;
                        })
                        .sorted((p1, p2) -> {
                            double d1 = p1.calculateDistance(latitude, longitude);
                            double d2 = p2.calculateDistance(latitude, longitude);
                            return Double.compare(d1, d2);
                        })
                        .collect(Collectors.toList());
            }
            
            // 제한 적용
            if (limit != null) {
                places = places.stream()
                        .limit(limit)
                        .collect(Collectors.toList());
            }
            
            // 응답 변환
            List<PlaceResponse> responses = places.stream()
                    .map(place -> {
                        double distance = latitude != null && longitude != null ? 
                                place.calculateDistance(latitude, longitude) : 0.0;
                        return PlaceResponse.from(place, distance);
                    })
                    .collect(Collectors.toList());
            
            log.info("태그 기반 장소 검색 완료 - {}개 장소 발견", responses.size());
            return responses;
            
        } catch (Exception e) {
            log.error("태그 기반 장소 검색 중 오류 발생", e);
            throw new BusinessException(ErrorCode.PLACE_SEARCH_FAILED, e);
        }
    }
    
    /**
     * 카테고리와 태그로 복합 검색
     */
    public List<PlaceResponse> searchPlacesByCategoryAndTags(PlaceCategory category, Set<PlaceTag> tags,
                                                           Double latitude, Double longitude, 
                                                           Integer radiusMeters, Integer limit) {
        try {
            log.info("카테고리-태그 복합 검색 시작 - 카테고리: {}, 태그: {}", category, tags);
            
            List<Place> places = placeRepository.findByCategoryAndTags(category, tags);
            
            // 위치 필터링
            if (latitude != null && longitude != null && radiusMeters != null) {
                places = places.stream()
                        .filter(place -> {
                            double distance = place.calculateDistance(latitude, longitude);
                            return distance <= radiusMeters;
                        })
                        .sorted((p1, p2) -> {
                            double d1 = p1.calculateDistance(latitude, longitude);
                            double d2 = p2.calculateDistance(latitude, longitude);
                            return Double.compare(d1, d2);
                        })
                        .collect(Collectors.toList());
            }
            
            // 제한 적용
            if (limit != null) {
                places = places.stream()
                        .limit(limit)
                        .collect(Collectors.toList());
            }
            
            // 응답 변환
            List<PlaceResponse> responses = places.stream()
                    .map(place -> {
                        double distance = latitude != null && longitude != null ? 
                                place.calculateDistance(latitude, longitude) : 0.0;
                        return PlaceResponse.from(place, distance);
                    })
                    .collect(Collectors.toList());
            
            log.info("카테고리-태그 복합 검색 완료 - {}개 장소 발견", responses.size());
            return responses;
            
        } catch (Exception e) {
            log.error("카테고리-태그 복합 검색 중 오류 발생", e);
            throw new BusinessException(ErrorCode.PLACE_SEARCH_FAILED, e);
        }
    }
    
    /**
     * 모든 태그를 포함하는 장소 검색 (AND 조건)
     */
    public List<PlaceResponse> searchPlacesByAllTags(Set<PlaceTag> tags, Double latitude, Double longitude,
                                                   Integer radiusMeters, Integer limit) {
        try {
            log.info("전체 태그 포함 검색 시작 - 태그: {}", tags);
            
            List<Place> places = placeRepository.findByAllTags(tags, tags.size());
            
            // 위치 필터링
            if (latitude != null && longitude != null && radiusMeters != null) {
                places = places.stream()
                        .filter(place -> {
                            double distance = place.calculateDistance(latitude, longitude);
                            return distance <= radiusMeters;
                        })
                        .sorted((p1, p2) -> {
                            double d1 = p1.calculateDistance(latitude, longitude);
                            double d2 = p2.calculateDistance(latitude, longitude);
                            return Double.compare(d1, d2);
                        })
                        .collect(Collectors.toList());
            }
            
            // 제한 적용
            if (limit != null) {
                places = places.stream()
                        .limit(limit)
                        .collect(Collectors.toList());
            }
            
            // 응답 변환
            List<PlaceResponse> responses = places.stream()
                    .map(place -> {
                        double distance = latitude != null && longitude != null ? 
                                place.calculateDistance(latitude, longitude) : 0.0;
                        return PlaceResponse.from(place, distance);
                    })
                    .collect(Collectors.toList());
            
            log.info("전체 태그 포함 검색 완료 - {}개 장소 발견", responses.size());
            return responses;
            
        } catch (Exception e) {
            log.error("전체 태그 포함 검색 중 오류 발생", e);
            throw new BusinessException(ErrorCode.PLACE_SEARCH_FAILED, e);
        }
    }

    /**
     * 추천 점수 계산 (추후 고도화)
     */
    public double calculateRecommendationScore(Place place, PlaceSearchRequest request) {
        double score = 0.0;
        
        // 거리 점수 (가까울수록 높은 점수)
        double distance = place.calculateDistance(request.getLatitude(), request.getLongitude());
        double distanceScore = Math.max(0, 100 - (distance / 100)); // 10km까지 점수 부여
        score += distanceScore * 0.3;
        
        // 평점 점수
        if (place.getRating() != null) {
            score += (place.getRating() / 5.0) * 100 * 0.4;
        }
        
        // 리뷰 수 점수
        if (place.getAdditionalInfo() != null && place.getAdditionalInfo().getReviewCount() != null) {
            double reviewScore = Math.min(100, place.getAdditionalInfo().getReviewCount() / 10.0);
            score += reviewScore * 0.3;
        }
        
        return Math.min(100, score);
    }
    
    /**
     * 카카오 API 응답을 Place 엔티티로 변환
     */
    private List<Place> convertKakaoResponseToPlaces(KakaoPlaceResponse response) {
        if (response == null || response.getDocuments() == null) {
            return new ArrayList<>();
        }
        
        return response.getDocuments().stream()
                .map(this::convertKakaoDocumentToPlace)
                .filter(place -> place != null)
                .collect(Collectors.toList());
    }
    
    /**
     * 카카오 API 문서를 Place 엔티티로 변환
     */
    private Place convertKakaoDocumentToPlace(KakaoPlaceResponse.Document doc) {
        try {
            return Place.builder()
                    .kakaoPlaceId(doc.getId())
                    .name(doc.getPlaceName())
                    .address(doc.getAddressName())
                    .roadAddress(doc.getRoadAddressName())
                    .latitude(Double.parseDouble(doc.getLatitude()))
                    .longitude(Double.parseDouble(doc.getLongitude()))
                    .category(mapKakaoCategoryToEnum(doc.getCategoryName()))
                    .phone(doc.getPhone())
                    .rating(null) // 카카오 API 기본 검색에는 평점 없음
                    .placeUrl(doc.getPlaceUrl())
                    .build();
        } catch (Exception e) {
            log.warn("카카오 장소 변환 실패: {}, 오류: {}", doc.getPlaceName(), e.getMessage());
            return null;
        }
    }
    
    /**
     * PlaceCategory를 카카오 카테고리 코드로 매핑
     */
    private String getCategoryCode(PlaceCategory category) {
        return switch (category) {
            case CAFE -> "CE7";
            case RESTAURANT -> "FD6";
            case STUDY_CAFE -> "CE7"; // 스터디카페는 카페로 분류
            case ENTERTAINMENT -> "CT1"; // 문화시설
            case PARK -> "AT4"; // 관광명소 (공원 포함)
            case SHOPPING -> "MT1"; // 대형마트
        };
    }
    
    /**
     * 카카오 카테고리를 PlaceCategory enum으로 매핑
     */
    private PlaceCategory mapKakaoCategoryToEnum(String kakaoCategory) {
        if (kakaoCategory == null) {
            return PlaceCategory.CAFE; // 기본값
        }
        
        String category = kakaoCategory.toLowerCase();
        
        if (category.contains("카페") || category.contains("cafe")) {
            return PlaceCategory.CAFE;
        } else if (category.contains("음식") || category.contains("식당") || category.contains("맛집")) {
            return PlaceCategory.RESTAURANT;
        } else if (category.contains("공원") || category.contains("관광")) {
            return PlaceCategory.PARK;
        } else if (category.contains("쇼핑") || category.contains("마트") || category.contains("매장")) {
            return PlaceCategory.SHOPPING;
        } else if (category.contains("문화") || category.contains("영화") || category.contains("놀이")) {
            return PlaceCategory.ENTERTAINMENT;
        } else {
            return PlaceCategory.CAFE; // 기본값
        }
    }
}