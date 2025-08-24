package com.loadmapguide_backend.global.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlaceCategory {
    
    CAFE("CE7", "카페"),
    RESTAURANT("FD6", "음식점"),
    STUDY_CAFE("STUDY", "스터디카페"),
    ENTERTAINMENT("CT1", "문화시설"),
    PARK("AT4", "공원"),
    SHOPPING("MT1", "대형마트");
    
    private final String kakaoCode;
    private final String description;
}