package com.loadmapguide_backend.domain.place.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PlaceAdditionalInfo {
    
    private List<String> menuItems;
    private List<String> facilities;
    private String parkingInfo;
    private Boolean wifiAvailable;
    private Boolean cardPayment;
    private String atmosphere;
    private Integer reviewCount;
    
    @Builder
    public PlaceAdditionalInfo(List<String> menuItems, List<String> facilities,
                             String parkingInfo, Boolean wifiAvailable, Boolean cardPayment,
                             String atmosphere, Integer reviewCount) {
        this.menuItems = menuItems;
        this.facilities = facilities;
        this.parkingInfo = parkingInfo;
        this.wifiAvailable = wifiAvailable;
        this.cardPayment = cardPayment;
        this.atmosphere = atmosphere;
        this.reviewCount = reviewCount;
    }
}