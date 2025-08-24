package com.loadmapguide_backend.domain.location.entity;

import com.loadmapguide_backend.global.common.enums.PlaceCategory;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PreferenceSettings {
    
    private Integer maxBudget;
    private List<PlaceCategory> preferredCategories;
    private String purpose;
    private Boolean indoorOnly;
    private Integer searchRadius;
    private String weatherConsideration;
    
    @Builder
    public PreferenceSettings(Integer maxBudget, List<PlaceCategory> preferredCategories,
                            String purpose, Boolean indoorOnly, Integer searchRadius,
                            String weatherConsideration) {
        this.maxBudget = maxBudget;
        this.preferredCategories = preferredCategories;
        this.purpose = purpose;
        this.indoorOnly = indoorOnly;
        this.searchRadius = searchRadius != null ? searchRadius : 1000; // 기본 1km
        this.weatherConsideration = weatherConsideration;
    }
}