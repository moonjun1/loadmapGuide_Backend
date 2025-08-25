package com.loadmapguide_backend.global.external.openai;

import com.loadmapguide_backend.global.external.openai.dto.CommercialScoreRequest;
import com.loadmapguide_backend.global.external.openai.dto.CommercialScoreResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiApiClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${openai.api.key}")
    private String apiKey;
    
    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;
    
    /**
     * GPT를 활용한 상업지역 점수 분석
     */
    public CommercialScoreResponse analyzeCommercialScore(CommercialScoreRequest request) {
        try {
            log.debug("GPT API 호출 - 위치: {}", request.getAddress());
            
            // GPT 프롬프트 생성
            String prompt = buildPrompt(request);
            
            // API 요청 생성
            Map<String, Object> requestBody = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(
                    Map.of("role", "system", "content", "당신은 서울의 상권 분석 전문가입니다. 주어진 위치의 상업지역 점수를 0-100점으로 평가해주세요."),
                    Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", 300,
                "temperature", 0.3
            );
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // API 호출
            ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl, 
                HttpMethod.POST, 
                entity, 
                Map.class
            );
            
            // 응답 파싱
            return parseResponse(response.getBody(), request.getAddress());
            
        } catch (Exception e) {
            log.warn("GPT API 호출 실패: {}", e.getMessage());
            // 기본 점수 반환
            return CommercialScoreResponse.builder()
                    .score(50.0)
                    .description("기본 상업지역")
                    .category("일반지역")
                    .confidence(0.5)
                    .build();
        }
    }
    
    /**
     * GPT 프롬프트 생성
     */
    private String buildPrompt(CommercialScoreRequest request) {
        return String.format(
            """
            다음 위치의 상업지역 점수를 분석해주세요:
            
            주소: %s
            위도: %.6f
            경도: %.6f
            
            다음 기준으로 0-100점으로 평가하고 JSON 형태로 응답해주세요:
            - 상업시설 밀도 (카페, 식당, 편의점, 쇼핑몰 등)
            - 대중교통 접근성 (지하철역, 버스정류장 근접성)
            - 유동인구 및 활동성
            - 업무/상업지구 특성
            
            응답 형식:
            {
              "score": 75.5,
              "description": "활발한 상업지역",
              "category": "주요상권",
              "reasons": ["지하철역 근접", "다양한 상업시설", "높은 유동인구"]
            }
            """,
            request.getAddress(),
            request.getLatitude(), 
            request.getLongitude()
        );
    }
    
    /**
     * GPT 응답 파싱
     */
    private CommercialScoreResponse parseResponse(Map<String, Object> response, String address) {
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("GPT 응답에서 choices가 없습니다.");
            }
            
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");
            
            log.debug("GPT 응답: {}", content);
            
            // JSON 파싱 시도
            if (content.contains("{") && content.contains("}")) {
                String jsonPart = content.substring(content.indexOf("{"), content.lastIndexOf("}") + 1);
                // 간단한 JSON 파싱 (실제로는 ObjectMapper 사용하는게 좋음)
                return parseJsonResponse(jsonPart);
            }
            
            // JSON 파싱 실패시 기본값
            return CommercialScoreResponse.builder()
                    .score(60.0)
                    .description("상업지역")
                    .category("일반지역")
                    .confidence(0.7)
                    .build();
                    
        } catch (Exception e) {
            log.warn("GPT 응답 파싱 실패: {}", e.getMessage());
            return CommercialScoreResponse.builder()
                    .score(50.0)
                    .description("기본 상업지역")
                    .category("일반지역")
                    .confidence(0.5)
                    .build();
        }
    }
    
    /**
     * 간단한 JSON 파싱 (개선 필요)
     */
    private CommercialScoreResponse parseJsonResponse(String json) {
        try {
            // 매우 간단한 파싱 (실제로는 ObjectMapper 사용 권장)
            double score = 60.0;
            String description = "상업지역";
            String category = "일반지역";
            
            if (json.contains("\"score\"")) {
                String scorePart = json.substring(json.indexOf("\"score\""));
                String scoreValue = scorePart.substring(scorePart.indexOf(":") + 1, scorePart.indexOf(",")).trim();
                score = Double.parseDouble(scoreValue);
            }
            
            if (json.contains("\"description\"")) {
                String descPart = json.substring(json.indexOf("\"description\""));
                int start = descPart.indexOf("\"", descPart.indexOf(":")) + 1;
                int end = descPart.indexOf("\"", start);
                if (end > start) {
                    description = descPart.substring(start, end);
                }
            }
            
            if (json.contains("\"category\"")) {
                String catPart = json.substring(json.indexOf("\"category\""));
                int start = catPart.indexOf("\"", catPart.indexOf(":")) + 1;
                int end = catPart.indexOf("\"", start);
                if (end > start) {
                    category = catPart.substring(start, end);
                }
            }
            
            return CommercialScoreResponse.builder()
                    .score(score)
                    .description(description)
                    .category(category)
                    .confidence(0.8)
                    .build();
                    
        } catch (Exception e) {
            log.warn("JSON 파싱 실패: {}", e.getMessage());
            return CommercialScoreResponse.builder()
                    .score(55.0)
                    .description("상업지역")
                    .category("일반지역")
                    .confidence(0.6)
                    .build();
        }
    }
}