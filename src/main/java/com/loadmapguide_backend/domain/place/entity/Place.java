package com.loadmapguide_backend.domain.place.entity;

import com.loadmapguide_backend.global.common.entity.BaseEntity;
import com.loadmapguide_backend.global.common.enums.PlaceCategory;
import com.loadmapguide_backend.global.common.enums.PlaceTag;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Set;

@Entity
@Table(name = "places")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "kakao_place_id", unique = true, length = 100)
    private String kakaoPlaceId;
    
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private PlaceCategory category;
    
    @Column(name = "address", length = 300)
    private String address;
    
    @Column(name = "road_address", length = 300)
    private String roadAddress;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "rating")
    private Double rating;
    
    @Column(name = "price_range")
    private Integer priceRange;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "business_hours", columnDefinition = "json")
    private BusinessHours businessHours;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "additional_info", columnDefinition = "json")
    private PlaceAdditionalInfo additionalInfo;
    
    @Column(name = "place_url", length = 500)
    private String placeUrl;
    
    @ElementCollection(targetClass = PlaceTag.class, fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
        name = "place_tags", 
        joinColumns = @JoinColumn(name = "place_id")
    )
    @Column(name = "tag")
    private Set<PlaceTag> tags;
    
    @Builder
    public Place(String kakaoPlaceId, String name, PlaceCategory category,
                String address, String roadAddress, Double latitude, Double longitude,
                String phone, Double rating, Integer priceRange,
                BusinessHours businessHours, PlaceAdditionalInfo additionalInfo,
                String placeUrl, Set<PlaceTag> tags) {
        this.kakaoPlaceId = kakaoPlaceId;
        this.name = name;
        this.category = category;
        this.address = address;
        this.roadAddress = roadAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone = phone;
        this.rating = rating;
        this.priceRange = priceRange;
        this.businessHours = businessHours;
        this.additionalInfo = additionalInfo;
        this.placeUrl = placeUrl;
        this.tags = tags;
    }
    
    /**
     * 태그 추가
     */
    public void addTag(PlaceTag tag) {
        if (this.tags == null) {
            this.tags = new java.util.HashSet<>();
        }
        this.tags.add(tag);
    }
    
    /**
     * 태그 제거
     */
    public void removeTag(PlaceTag tag) {
        if (this.tags != null) {
            this.tags.remove(tag);
        }
    }
    
    /**
     * 특정 태그가 있는지 확인
     */
    public boolean hasTag(PlaceTag tag) {
        return this.tags != null && this.tags.contains(tag);
    }
    
    public void updateRating(Double rating) {
        this.rating = rating;
    }
    
    public void updateBusinessHours(BusinessHours businessHours) {
        this.businessHours = businessHours;
    }
    
    /**
     * 실시간 정보 업데이트 (카카오 API에서 가져온 최신 정보로 업데이트)
     */
    public void updateRealTimeInfo(String name, String phone, Double rating, 
                                  BusinessHours businessHours, PlaceAdditionalInfo additionalInfo) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
        if (phone != null && !phone.trim().isEmpty()) {
            this.phone = phone;
        }
        if (rating != null && rating > 0) {
            this.rating = rating;
        }
        if (businessHours != null) {
            this.businessHours = businessHours;
        }
        if (additionalInfo != null) {
            this.additionalInfo = additionalInfo;
        }
    }
    
    public double calculateDistance(Double targetLat, Double targetLng) {
        if (this.latitude == null || this.longitude == null) {
            return Double.MAX_VALUE;
        }
        
        // Haversine formula for distance calculation
        final int R = 6371; // Earth's radius in km
        
        double latDistance = Math.toRadians(targetLat - this.latitude);
        double lonDistance = Math.toRadians(targetLng - this.longitude);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(targetLat))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
                
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c * 1000; // Convert to meters
    }
}