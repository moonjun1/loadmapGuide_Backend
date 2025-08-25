package com.loadmapguide_backend.global.external.openai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommercialScoreResponse {
    
    private double score;           // 0-100 점수
    private String description;     // 상업지역 설명
    private String category;        // 지역 카테고리
    private double confidence;      // 신뢰도 (0-1)
    private List<String> reasons;   // 점수 산정 이유들
}