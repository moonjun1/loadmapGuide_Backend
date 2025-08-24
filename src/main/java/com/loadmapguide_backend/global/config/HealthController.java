package com.loadmapguide_backend.global.config;

import com.loadmapguide_backend.global.common.dto.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/health")
public class HealthController {
    
    @GetMapping
    public BaseResponse<Map<String, Object>> healthCheck() {
        log.info("Health check requested at {}", LocalDateTime.now());
        
        Map<String, Object> healthData = Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now(),
            "service", "LoadMap Guide Backend",
            "version", "1.0.0"
        );
        
        return BaseResponse.success("서비스가 정상 동작중입니다.", healthData);
    }
}