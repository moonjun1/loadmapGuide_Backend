package com.loadmapguide_backend.global.external.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class KakaoPlaceResponse {
    
    @JsonProperty("meta")
    private Meta meta;
    
    @JsonProperty("documents")
    private List<Document> documents;
    
    @Getter
    @NoArgsConstructor
    public static class Meta {
        @JsonProperty("total_count")
        private Integer totalCount;
        
        @JsonProperty("pageable_count")
        private Integer pageableCount;
        
        @JsonProperty("is_end")
        private Boolean isEnd;
        
        @JsonProperty("same_name")
        private SameName sameName;
        
        @Getter
        @NoArgsConstructor
        public static class SameName {
            @JsonProperty("region")
            private List<String> region;
            
            @JsonProperty("keyword")
            private String keyword;
            
            @JsonProperty("selected_region")
            private String selectedRegion;
        }
    }
    
    @Getter
    @NoArgsConstructor
    public static class Document {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("place_name")
        private String placeName;
        
        @JsonProperty("category_name")
        private String categoryName;
        
        @JsonProperty("category_group_code")
        private String categoryGroupCode;
        
        @JsonProperty("category_group_name")
        private String categoryGroupName;
        
        @JsonProperty("phone")
        private String phone;
        
        @JsonProperty("address_name")
        private String addressName;
        
        @JsonProperty("road_address_name")
        private String roadAddressName;
        
        @JsonProperty("x")
        private String longitude;
        
        @JsonProperty("y")
        private String latitude;
        
        @JsonProperty("place_url")
        private String placeUrl;
        
        @JsonProperty("distance")
        private String distance;
    }
    
    public boolean hasResults() {
        return documents != null && !documents.isEmpty();
    }
    
    public List<Document> getResults() {
        return documents != null ? documents : List.of();
    }
}