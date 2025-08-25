package com.loadmapguide_backend.global.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransportationType {
    
    CAR("CAR", "자동차"),
    SUBWAY("SUBWAY", "지하철"),
    BUS("BUS", "버스"),
    PUBLIC_TRANSPORT("PUBLIC_TRANSPORT", "대중교통"),
    WALK("WALK", "도보");
    
    private final String code;
    private final String description;
}