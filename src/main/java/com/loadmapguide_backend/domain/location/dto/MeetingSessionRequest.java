package com.loadmapguide_backend.domain.location.dto;

import com.loadmapguide_backend.global.common.enums.PlaceCategory;
import com.loadmapguide_backend.global.common.enums.TransportationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class MeetingSessionRequest {
    
    @NotBlank(message = "세션명은 필수입니다")
    private String sessionName;
    
    @NotEmpty(message = "출발지 목록이 필요합니다")
    @Valid
    private List<LocationRequest> startLocations;
    
    private TransportationType transportationType = TransportationType.PUBLIC_TRANSPORT;
    
    @Min(value = 5000, message = "최소 예산은 5000원입니다")
    @Max(value = 100000, message = "최대 예산은 100000원입니다")
    private Integer maxBudget;
    
    private List<PlaceCategory> preferredCategories;
    
    private String purpose;
    
    private Boolean indoorOnly = false;
    
    @Min(value = 500, message = "최소 검색 반경은 500m입니다")
    @Max(value = 5000, message = "최대 검색 반경은 5000m입니다")
    private Integer searchRadius = 1000;
    
    private Boolean considerWeather = true;
    
    @Builder
    public MeetingSessionRequest(String sessionName, List<LocationRequest> startLocations,
                               TransportationType transportationType, Integer maxBudget,
                               List<PlaceCategory> preferredCategories, String purpose,
                               Boolean indoorOnly, Integer searchRadius, Boolean considerWeather) {
        this.sessionName = sessionName;
        this.startLocations = startLocations;
        this.transportationType = transportationType;
        this.maxBudget = maxBudget;
        this.preferredCategories = preferredCategories;
        this.purpose = purpose;
        this.indoorOnly = indoorOnly;
        this.searchRadius = searchRadius;
        this.considerWeather = considerWeather;
    }
    
    public Integer getParticipantCount() {
        return startLocations != null ? startLocations.size() : 0;
    }
}