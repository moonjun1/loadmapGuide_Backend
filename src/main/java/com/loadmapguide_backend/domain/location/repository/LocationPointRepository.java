package com.loadmapguide_backend.domain.location.repository;

import com.loadmapguide_backend.domain.location.entity.LocationPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationPointRepository extends JpaRepository<LocationPoint, Long> {
    
    Optional<LocationPoint> findByAddressAndLatitudeAndLongitude(
            String address, Double latitude, Double longitude);
    
    @Query("SELECT lp FROM LocationPoint lp WHERE " +
           "lp.latitude BETWEEN :minLat AND :maxLat AND " +
           "lp.longitude BETWEEN :minLng AND :maxLng")
    List<LocationPoint> findLocationPointsInArea(
            @Param("minLat") Double minLatitude,
            @Param("maxLat") Double maxLatitude,
            @Param("minLng") Double minLongitude,
            @Param("maxLng") Double maxLongitude);
    
    @Query(value = 
           "SELECT *, " +
           "ST_Distance_Sphere(POINT(longitude, latitude), POINT(:lng, :lat)) as distance " +
           "FROM location_points " +
           "HAVING distance <= :radiusMeters " +
           "ORDER BY distance",
           nativeQuery = true)
    List<LocationPoint> findNearbyLocations(
            @Param("lat") Double latitude,
            @Param("lng") Double longitude,
            @Param("radiusMeters") Double radiusMeters);
}