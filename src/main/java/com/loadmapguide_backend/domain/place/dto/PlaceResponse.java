package com.loadmapguide_backend.domain.place.dto;

import com.loadmapguide_backend.domain.place.entity.Place;
import com.loadmapguide_backend.global.common.enums.PlaceCategory;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PlaceResponse {
    
    private Long id;
    private String kakaoPlaceId;
    private String name;
    private PlaceCategory category;
    private String address;
    private String roadAddress;
    private Double latitude;
    private Double longitude;
    private String phone;
    private Double rating;
    private Integer priceRange;
    private String placeUrl;
    private Double distanceMeters;
    private BusinessHoursInfo businessHours;
    private PlaceDetailInfo additionalInfo;
    
    public static PlaceResponse from(Place place) {
        return PlaceResponse.builder()
                .id(place.getId())
                .kakaoPlaceId(place.getKakaoPlaceId())
                .name(place.getName())
                .category(place.getCategory())
                .address(place.getAddress())
                .roadAddress(place.getRoadAddress())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .phone(place.getPhone())
                .rating(place.getRating())
                .priceRange(place.getPriceRange())
                .placeUrl(place.getPlaceUrl())
                .businessHours(BusinessHoursInfo.from(place.getBusinessHours()))
                .additionalInfo(PlaceDetailInfo.from(place.getAdditionalInfo()))
                .build();
    }
    
    public static PlaceResponse from(Place place, Double distanceMeters) {
        PlaceResponse response = from(place);
        return PlaceResponse.builder()
                .id(response.getId())
                .kakaoPlaceId(response.getKakaoPlaceId())
                .name(response.getName())
                .category(response.getCategory())
                .address(response.getAddress())
                .roadAddress(response.getRoadAddress())
                .latitude(response.getLatitude())
                .longitude(response.getLongitude())
                .phone(response.getPhone())
                .rating(response.getRating())
                .priceRange(response.getPriceRange())
                .placeUrl(response.getPlaceUrl())
                .distanceMeters(distanceMeters)
                .businessHours(response.getBusinessHours())
                .additionalInfo(response.getAdditionalInfo())
                .build();
    }
    
    @Getter
    @Builder
    public static class BusinessHoursInfo {
        private String todayHours;
        private Boolean isOpenNow;
        private String specialNote;
        
        public static BusinessHoursInfo from(com.loadmapguide_backend.domain.place.entity.BusinessHours businessHours) {
            if (businessHours == null) {
                return BusinessHoursInfo.builder()
                        .todayHours("정보 없음")
                        .isOpenNow(null)
                        .specialNote("")
                        .build();
            }
            
            return BusinessHoursInfo.builder()
                    .todayHours("09:00-22:00") // 임시값
                    .isOpenNow(true) // 임시값
                    .specialNote(businessHours.getSpecialNote())
                    .build();
        }
    }
    
    @Getter
    @Builder
    public static class PlaceDetailInfo {
        private List<String> menuItems;
        private List<String> facilities;
        private String parkingInfo;
        private Boolean wifiAvailable;
        private Boolean cardPayment;
        private String atmosphere;
        private Integer reviewCount;
        
        public static PlaceDetailInfo from(com.loadmapguide_backend.domain.place.entity.PlaceAdditionalInfo additionalInfo) {
            if (additionalInfo == null) {
                return PlaceDetailInfo.builder()
                        .menuItems(List.of())
                        .facilities(List.of())
                        .parkingInfo("정보 없음")
                        .wifiAvailable(null)
                        .cardPayment(null)
                        .atmosphere("정보 없음")
                        .reviewCount(0)
                        .build();
            }
            
            return PlaceDetailInfo.builder()
                    .menuItems(additionalInfo.getMenuItems())
                    .facilities(additionalInfo.getFacilities())
                    .parkingInfo(additionalInfo.getParkingInfo())
                    .wifiAvailable(additionalInfo.getWifiAvailable())
                    .cardPayment(additionalInfo.getCardPayment())
                    .atmosphere(additionalInfo.getAtmosphere())
                    .reviewCount(additionalInfo.getReviewCount())
                    .build();
        }
    }
}