package com.loadmapguide_backend.global.external.openai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommercialScoreRequest {
    
    private String address;
    private double latitude;
    private double longitude;
    private String context; // 추가 컨텍스트 (선택사항)
}