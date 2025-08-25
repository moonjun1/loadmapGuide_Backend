package com.loadmapguide_backend.domain.place.repository;

import com.loadmapguide_backend.domain.place.entity.Place;
import com.loadmapguide_backend.global.common.enums.PlaceCategory;
import com.loadmapguide_backend.global.common.enums.PlaceTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
    
    Optional<Place> findByKakaoPlaceId(String kakaoPlaceId);
    
    List<Place> findByCategory(PlaceCategory category);
    
    List<Place> findByNameContaining(String name);
    
    @Query("SELECT p FROM Place p WHERE " +
           "p.latitude BETWEEN :minLat AND :maxLat AND " +
           "p.longitude BETWEEN :minLng AND :maxLng")
    List<Place> findPlacesInArea(
            @Param("minLat") Double minLatitude,
            @Param("maxLat") Double maxLatitude,
            @Param("minLng") Double minLongitude,
            @Param("maxLng") Double maxLongitude);
    
    @Query("SELECT p FROM Place p WHERE " +
           "p.category = :category AND " +
           "p.latitude BETWEEN :minLat AND :maxLat AND " +
           "p.longitude BETWEEN :minLng AND :maxLng")
    List<Place> findPlacesByCategoryInArea(
            @Param("category") PlaceCategory category,
            @Param("minLat") Double minLatitude,
            @Param("maxLat") Double maxLatitude,
            @Param("minLng") Double minLongitude,
            @Param("maxLng") Double maxLongitude);
    
    @Query("SELECT p FROM Place p WHERE p.rating >= :minRating ORDER BY p.rating DESC")
    List<Place> findPlacesByMinRating(@Param("minRating") Double minRating);
    
    @Query(value = 
           "SELECT *, " +
           "ST_Distance_Sphere(POINT(longitude, latitude), POINT(:lng, :lat)) as distance " +
           "FROM places " +
           "WHERE (:category IS NULL OR category = :category) " +
           "HAVING distance <= :radiusMeters " +
           "ORDER BY distance, rating DESC",
           nativeQuery = true)
    List<Place> findNearbyPlaces(
            @Param("lat") Double latitude,
            @Param("lng") Double longitude,
            @Param("radiusMeters") Double radiusMeters,
            @Param("category") String category);
    
    /**
     * 태그 기반 장소 검색
     */
    @Query("SELECT DISTINCT p FROM Place p JOIN p.tags t WHERE t IN :tags")
    List<Place> findByTagsIn(@Param("tags") Set<PlaceTag> tags);
    
    /**
     * 모든 태그를 포함하는 장소 검색 (AND 조건)
     */
    @Query("SELECT p FROM Place p WHERE SIZE(p.tags) >= :tagCount AND " +
           "(SELECT COUNT(t) FROM Place p2 JOIN p2.tags t WHERE p2.id = p.id AND t IN :tags) = :tagCount")
    List<Place> findByAllTags(@Param("tags") Set<PlaceTag> tags, @Param("tagCount") int tagCount);
    
    /**
     * 특정 위치 근처에서 태그로 검색
     */
    @Query("SELECT DISTINCT p FROM Place p JOIN p.tags t WHERE " +
           "t IN :tags AND " +
           "p.latitude BETWEEN :minLat AND :maxLat AND " +
           "p.longitude BETWEEN :minLng AND :maxLng " +
           "ORDER BY p.rating DESC")
    List<Place> findByTagsInArea(
            @Param("tags") Set<PlaceTag> tags,
            @Param("minLat") Double minLatitude,
            @Param("maxLat") Double maxLatitude,
            @Param("minLng") Double minLongitude,
            @Param("maxLng") Double maxLongitude);
    
    /**
     * 카테고리와 태그로 복합 검색
     */
    @Query("SELECT DISTINCT p FROM Place p JOIN p.tags t WHERE " +
           "p.category = :category AND t IN :tags " +
           "ORDER BY p.rating DESC")
    List<Place> findByCategoryAndTags(
            @Param("category") PlaceCategory category,
            @Param("tags") Set<PlaceTag> tags);
}