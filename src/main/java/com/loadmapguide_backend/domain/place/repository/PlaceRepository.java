package com.loadmapguide_backend.domain.place.repository;

import com.loadmapguide_backend.domain.place.entity.Place;
import com.loadmapguide_backend.global.common.enums.PlaceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
}