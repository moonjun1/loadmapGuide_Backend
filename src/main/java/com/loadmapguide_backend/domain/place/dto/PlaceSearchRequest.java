package com.loadmapguide_backend.domain.place.dto;

import com.loadmapguide_backend.global.common.enums.PlaceCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PlaceSearchRequest {
    
    @NotNull(message = "위도는 필수입니다")
    private Double latitude;
    
    @NotNull(message = "경도는 필수입니다")
    private Double longitude;
    
    @Min(value = 100, message = "최소 검색 반경은 100m입니다")
    @Max(value = 10000, message = "최대 검색 반경은 10000m입니다")
    private Integer radiusMeters = 1000;
    
    private List<PlaceCategory> categories;
    
    @Min(value = 0, message = "최소 예산은 0원입니다")
    @Max(value = 500000, message = "최대 예산은 500000원입니다")
    private Integer maxBudget;
    
    @Min(value = 0, message = "최소 평점은 0점입니다")
    @Max(value = 5, message = "최대 평점은 5점입니다")
    private Double minRating;
    
    private String keyword;
    
    private Boolean openNow;
    
    private String sortBy = "DISTANCE"; // DISTANCE, RATING, POPULARITY
    
    @Min(value = 1, message = "최소 결과 수는 1개입니다")
    @Max(value = 50, message = "최대 결과 수는 50개입니다")
    private Integer limit = 20;
    
    @Builder
    public PlaceSearchRequest(Double latitude, Double longitude, Integer radiusMeters,
                            List<PlaceCategory> categories, Integer maxBudget, Double minRating,
                            String keyword, Boolean openNow, String sortBy, Integer limit) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusMeters = radiusMeters;
        this.categories = categories;
        this.maxBudget = maxBudget;
        this.minRating = minRating;
        this.keyword = keyword;
        this.openNow = openNow;
        this.sortBy = sortBy;
        this.limit = limit;
    }
}