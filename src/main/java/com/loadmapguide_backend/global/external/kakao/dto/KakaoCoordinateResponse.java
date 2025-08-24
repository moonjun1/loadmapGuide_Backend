package com.loadmapguide_backend.global.external.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class KakaoCoordinateResponse {
    
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
    }
    
    @Getter
    @NoArgsConstructor
    public static class Document {
        @JsonProperty("address_name")
        private String addressName;
        
        @JsonProperty("y")
        private String latitude;
        
        @JsonProperty("x")
        private String longitude;
        
        @JsonProperty("address_type")
        private String addressType;
        
        @JsonProperty("address")
        private Address address;
        
        @JsonProperty("road_address")
        private RoadAddress roadAddress;
        
        @Getter
        @NoArgsConstructor
        public static class Address {
            @JsonProperty("address_name")
            private String addressName;
            
            @JsonProperty("region_1depth_name")
            private String region1depthName;
            
            @JsonProperty("region_2depth_name")
            private String region2depthName;
            
            @JsonProperty("region_3depth_name")
            private String region3depthName;
            
            @JsonProperty("mountain_yn")
            private String mountainYn;
            
            @JsonProperty("main_address_no")
            private String mainAddressNo;
            
            @JsonProperty("sub_address_no")
            private String subAddressNo;
        }
        
        @Getter
        @NoArgsConstructor
        public static class RoadAddress {
            @JsonProperty("address_name")
            private String addressName;
            
            @JsonProperty("region_1depth_name")
            private String region1depthName;
            
            @JsonProperty("region_2depth_name")
            private String region2depthName;
            
            @JsonProperty("region_3depth_name")
            private String region3depthName;
            
            @JsonProperty("road_name")
            private String roadName;
            
            @JsonProperty("underground_yn")
            private String undergroundYn;
            
            @JsonProperty("main_building_no")
            private String mainBuildingNo;
            
            @JsonProperty("sub_building_no")
            private String subBuildingNo;
            
            @JsonProperty("building_name")
            private String buildingName;
        }
    }
    
    public boolean hasResults() {
        return documents != null && !documents.isEmpty();
    }
    
    public Document getFirstResult() {
        return hasResults() ? documents.get(0) : null;
    }
}