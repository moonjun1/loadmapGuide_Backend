package com.loadmapguide_backend.domain.location.entity;

import com.loadmapguide_backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "location_points")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LocationPoint extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "address", nullable = false, length = 300)
    private String address;
    
    @Column(name = "latitude", nullable = false)
    private Double latitude;
    
    @Column(name = "longitude", nullable = false)
    private Double longitude;
    
    @Column(name = "place_name", length = 200)
    private String placeName;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Builder
    public LocationPoint(String address, Double latitude, Double longitude, 
                        String placeName, String description) {
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.placeName = placeName;
        this.description = description;
    }
    
    public void updateCoordinates(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}