package com.loadmapguide_backend.domain.location.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LocationRequest {
    
    @NotBlank(message = "주소는 필수입니다")
    private String address;
    
    private Double latitude;
    private Double longitude;
    
    private String placeName;
    private String participantName;
    
    @Builder
    public LocationRequest(String address, Double latitude, Double longitude,
                         String placeName, String participantName) {
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.placeName = placeName;
        this.participantName = participantName;
    }
    
    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }
}