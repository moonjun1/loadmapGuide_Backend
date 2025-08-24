# 중간지점 만남 서비스 기획서

## 📋 프로젝트 개요

### 서비스 컨셉
여러 명이 만날 때 모두에게 공평한 중간지점을 찾고, 목적에 맞는 장소를 추천해주는 웹 서비스

### 핵심 가치 제안
- **공평성**: 모든 참가자의 이동시간을 최소화하는 중간지점 계산
- **편의성**: 목적별 맞춤 장소 추천 (카페/놀이/공부)
- **실용성**: 실시간 정보 연동 (날씨, 영업시간, 예산)

---

## 🎯 MVP (Minimum Viable Product) 기능

### Phase 1: 핵심 기능 (2-3주)
- [x] **출발지 입력**: 2-6명의 출발지 입력 (주소/지하철역)
- [x] **중간지점 계산**: 대중교통 기준 도착시간 계산 알고리즘
- [x] **지도 연동**: 카카오맵 API를 통한 위치 시각화
- [x] **기본 장소 추천**: 중간지점 반경 1km 내 카페/식당 검색

### Phase 2: 필터링 & 개선 (2주)
- [x] **교통수단 선택**: 대중교통/자동차/도보 옵션
- [x] **예산별 필터링**: 1만원/2만원/3만원/무제한 선택
- [x] **상세 정보 표시**: 영업시간, 평점, 메뉴 정보
- [x] **실내/실외 선택**: 기본적인 공간 타입 필터

### Phase 3: 고도화 기능 (2주)
- [x] **날씨 연동**: 실시간 날씨 기반 실내/실외 자동 추천
- [x] **목적별 세분화**: 놀이/공부/대화 등 세부 카테고리
- [x] **결과 공유**: 카카오톡 공유, URL 링크 생성
- [x] **즐겨찾기**: 자주 만나는 그룹 설정 저장

---

## 🏗️ 시스템 아키텍처

### 전체 시스템 구조
```
[Client Browser] ←→ [Frontend (React)] ←→ [Backend (Spring Boot)] ←→ [Database & Cache]
                                                      ↓
                                            [External APIs]
```

---

## 🖥️ 프론트엔드 아키텍처

### 기술 스택
```
React 18 + TypeScript
├── 번들러: Vite 5.x
├── 패키지 매니저: npm/yarn
├── Node.js: 18.x LTS
└── 개발환경: VS Code + Extensions
```

### UI/UX 라이브러리
```
스타일링 & 컴포넌트
├── CSS 프레임워크: Tailwind CSS 3.x
├── 컴포넌트: shadcn/ui (Radix UI 기반)
├── 아이콘: Lucide React
├── 애니메이션: Framer Motion
└── 차트: Recharts (필요시)
```

### 상태 관리 & 데이터
```
상태 관리
├── 전역 상태: Zustand (경량화)
├── 서버 상태: TanStack Query (React Query)
├── 폼 상태: React Hook Form + Zod
└── 지도 상태: Kakao Map API 직접 관리
```

### HTTP 클라이언트 & 유틸
```
네트워킹 & 유틸리티
├── HTTP Client: Axios
├── 날짜 처리: date-fns
├── 유틸리티: lodash-es
└── 타입 검증: Zod
```

### 프론트엔드 폴더 구조
```
src/
├── components/           # 재사용 컴포넌트
│   ├── ui/              # shadcn/ui 컴포넌트
│   ├── common/          # 공통 컴포넌트 (Header, Footer)
│   └── features/        # 기능별 컴포넌트
│       ├── map/         # 지도 관련
│       ├── location/    # 위치 입력
│       └── place/       # 장소 추천
├── pages/               # 페이지 컴포넌트
├── hooks/               # 커스텀 훅
├── stores/              # Zustand 스토어
├── services/            # API 서비스 함수
├── types/               # TypeScript 타입 정의
├── utils/               # 유틸리티 함수
└── constants/           # 상수 정의
```

### 주요 컴포넌트 설계
```typescript
// 메인 페이지 컴포넌트 구조
components/
├── MapContainer.tsx        # 카카오맵 컨테이너
├── LocationInput.tsx       # 출발지 입력 폼
├── OptionsPanel.tsx        # 옵션 선택 (교통수단, 예산 등)
├── ResultsList.tsx         # 추천 장소 리스트
├── PlaceDetail.tsx         # 장소 상세 정보
└── ShareModal.tsx          # 결과 공유 모달
```

---

## ⚙️ 백엔드 아키텍처

### 기술 스택
```
Spring Boot 3.2.x
├── Java: OpenJDK 17 LTS
├── 빌드 도구: Gradle 8.x
├── 패키지 매니저: Gradle Wrapper
└── 개발환경: IntelliJ IDEA
```

### 프레임워크 & 라이브러리
```
Spring Ecosystem
├── Spring Web: REST API 개발
├── Spring Data JPA: 데이터 액세스
├── Spring Security: 인증/인가
├── Spring Cache: 캐싱 추상화
├── Spring Validation: 데이터 검증
└── Spring Boot Actuator: 모니터링
```

### 데이터베이스 & 캐싱
```
데이터 레이어
├── 메인 DB: PostgreSQL 15.x
├── 캐시: Redis 7.x
├── 연결풀: HikariCP
├── 마이그레이션: Flyway
└── 테스트 DB: H2 (인메모리)
```

### 외부 연동 & 유틸리티
```
외부 연동
├── HTTP Client: WebClient (Spring WebFlux)
├── JSON 처리: Jackson
├── 로깅: Logback + SLF4J
├── 문서화: SpringDoc OpenAPI 3
└── 테스트: JUnit 5 + TestContainers
```

### 백엔드 패키지 구조 (Domain + Module 혼합)
```
src/main/java/com/meetingpoint/
├── domain/                          # 도메인별 비즈니스 로직
│   ├── location/                    # 위치 관련 도메인
│   │   ├── controller/
│   │   │   └── LocationController.java
│   │   ├── service/
│   │   │   ├── LocationService.java
│   │   │   ├── MiddlePointCalculator.java
│   │   │   └── RouteCalculationService.java
│   │   ├── repository/
│   │   │   └── LocationHistoryRepository.java
│   │   ├── entity/
│   │   │   ├── LocationPoint.java
│   │   │   └── RouteInfo.java
│   │   └── dto/
│   │       ├── LocationRequest.java
│   │       └── MiddlePointResponse.java
│   │
│   ├── place/                       # 장소 관련 도메인
│   │   ├── controller/
│   │   │   └── PlaceController.java
│   │   ├── service/
│   │   │   ├── PlaceSearchService.java
│   │   │   ├── PlaceRecommendationService.java
│   │   │   └── PlaceCacheService.java
│   │   ├── repository/
│   │   │   └── PlaceCacheRepository.java
│   │   ├── entity/
│   │   │   ├── Place.java
│   │   │   └── PlaceCache.java
│   │   └── dto/
│   │       ├── PlaceSearchRequest.java
│   │       └── PlaceRecommendation.java
│   │
│   ├── session/                     # 세션 관리 도메인
│   │   ├── controller/
│   │   │   └── SessionController.java
│   │   ├── service/
│   │   │   ├── SessionService.java
│   │   │   └── UserGroupService.java
│   │   ├── repository/
│   │   │   ├── MeetingSessionRepository.java
│   │   │   └── UserGroupRepository.java
│   │   ├── entity/
│   │   │   ├── MeetingSession.java
│   │   │   └── UserGroup.java
│   │   └── dto/
│   │       ├── SessionRequest.java
│   │       └── SessionResponse.java
│   │
│   └── recommendation/              # 추천 시스템 도메인
│       ├── controller/
│       │   └── RecommendationController.java
│       ├── service/
│       │   ├── WeatherBasedRecommendationService.java
│       │   ├── PurposeBasedRecommendationService.java
│       │   └── PersonalizedRecommendationService.java
│       ├── entity/
│       │   └── RecommendationHistory.java
│       └── dto/
│           ├── RecommendationRequest.java
│           └── RecommendationResponse.java
│
├── global/                          # 공통 기능
│   ├── config/                      # 설정 클래스
│   │   ├── WebConfig.java
│   │   ├── SecurityConfig.java
│   │   ├── RedisConfig.java
│   │   └── JpaConfig.java
│   ├── exception/                   # 예외 처리
│   │   ├── GlobalExceptionHandler.java
│   │   ├── BusinessException.java
│   │   └── ErrorCode.java
│   ├── util/                        # 유틸리티
│   │   ├── GeoUtils.java
│   │   ├── DateTimeUtils.java
│   │   └── ValidationUtils.java
│   ├── external/                    # 외부 API 연동
│   │   ├── kakao/
│   │   │   ├── KakaoMapApiClient.java
│   │   │   ├── KakaoMobilityApiClient.java
│   │   │   └── dto/
│   │   ├── naver/
│   │   │   ├── NaverDirectionApiClient.java
│   │   │   └── dto/
│   │   └── weather/
│   │       ├── WeatherApiClient.java
│   │       └── dto/
│   └── common/                      # 공통 DTO, Enum
│       ├── dto/
│       │   ├── BaseResponse.java
│       │   └── PageRequest.java
│       └── enums/
│           ├── TransportationType.java
│           ├── PlaceCategory.java
│           └── PurposeType.java
│
└── MeetingPointApplication.java     # 메인 클래스
```

### 주요 서비스 클래스 설계
```java
// 중간지점 계산 서비스
@Service
@RequiredArgsConstructor
public class MiddlePointCalculator {
    
    private final RouteCalculationService routeService;
    private final PlaceSearchService placeSearchService;
    private final KakaoMapApiClient kakaoMapClient;
    
    /**
     * 최적 중간지점 계산
     * @param startPoints 출발지 목록
     * @param transport 교통수단
     * @return 최적 중간지점 목록 (상위 3-5개)
     */
    public List<OptimalLocation> calculateOptimalMeetingPoints(
        List<LocationRequest> startPoints,
        TransportationType transport
    ) {
        // 1. 각 출발지 좌표 변환
        List<LocationPoint> coordinates = convertToCoordinates(startPoints);
        
        // 2. 기하학적 중심점 계산
        LocationPoint geometricCenter = calculateGeometricCenter(coordinates);
        
        // 3. 주변 교통 허브 탐색
        List<LocationPoint> transportHubs = findNearbyTransportHubs(
            geometricCenter, transport);
        
        // 4. 각 후보지점에서의 이동시간 계산
        List<OptimalLocation> candidates = new ArrayList<>();
        for (LocationPoint hub : transportHubs) {
            double totalTravelTime = calculateTotalTravelTime(
                coordinates, hub, transport);
            double commercialScore = calculateCommercialScore(hub);
            
            candidates.add(OptimalLocation.builder()
                .location(hub)
                .averageTravelTime(totalTravelTime)
                .commercialScore(commercialScore)
                .overallScore(calculateOverallScore(totalTravelTime, commercialScore))
                .build());
        }
        
        // 5. 점수 기반 정렬 후 상위 반환
        return candidates.stream()
            .sorted(Comparator.comparingDouble(OptimalLocation::getOverallScore).reversed())
            .limit(5)
            .collect(Collectors.toList());
    }
    
    /**
     * 가중치 기반 점수 계산
     */
    private double calculateOverallScore(double travelTime, double commercialScore) {
        // 이동시간이 짧을수록 높은 점수 (60% 가중치)
        double timeScore = Math.max(0, 100 - travelTime * 2) * 0.6;
        // 상업지역 점수 (40% 가중치)  
        double commercialWeight = commercialScore * 0.4;
        
        return timeScore + commercialWeight;
    }
}

// 날씨 기반 추천 서비스
@Service
@RequiredArgsConstructor
public class WeatherBasedRecommendationService {
    
    private final WeatherApiClient weatherClient;
    private final PlaceSearchService placeSearchService;
    
    /**
     * 날씨 기반 장소 추천
     */
    public List<PlaceRecommendation> getWeatherOptimizedRecommendations(
        LocationPoint center, 
        RecommendationRequest request
    ) {
        WeatherInfo currentWeather = weatherClient.getCurrentWeather(center);
        
        // 날씨 조건에 따른 필터링
        PlaceCategory preferredCategory = determinePreferredCategory(
            currentWeather, request.getPurpose());
            
        List<Place> places = placeSearchService.searchNearbyPlaces(
            center, request.getRadius(), preferredCategory);
        
        return places.stream()
            .map(place -> buildRecommendation(place, currentWeather, request))
            .filter(Objects::nonNull)
            .sorted(Comparator.comparingDouble(PlaceRecommendation::getScore).reversed())
            .collect(Collectors.toList());
    }
    
    /**
     * 날씨 조건별 선호 카테고리 결정
     */
    private PlaceCategory determinePreferredCategory(
        WeatherInfo weather, 
        PurposeType purpose
    ) {
        // 비/눈 → 실내 공간 우선
        if (weather.getPrecipitationProbability() > 70) {
            return switch (purpose) {
                case STUDY -> PlaceCategory.STUDY_CAFE;
                case ENTERTAINMENT -> PlaceCategory.INDOOR_ENTERTAINMENT;
                default -> PlaceCategory.INDOOR_CAFE;
            };
        }
        
        // 폭염 (30도 이상) → 에어컨 잘 나오는 곳
        if (weather.getTemperature() > 30) {
            return PlaceCategory.AIR_CONDITIONED_SPACE;
        }
        
        // 한파 (5도 이하) → 따뜻한 실내
        if (weather.getTemperature() < 5) {
            return PlaceCategory.WARM_INDOOR_SPACE;
        }
        
        // 미세먼지 나쁨 → 실내 공간
        if (weather.getAirQualityIndex() > 150) {
            return PlaceCategory.INDOOR_SPACE_WITH_AIR_PURIFIER;
        }
        
        // 좋은 날씨 → 야외/테라스 포함
        return PlaceCategory.OUTDOOR_FRIENDLY;
    }
}

// 세션 관리 서비스
@Service
@RequiredArgsConstructor  
@Transactional
public class SessionService {
    
    private final MeetingSessionRepository sessionRepository;
    private final UserGroupRepository groupRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 새로운 세션 생성
     */
    public SessionResponse createSession(SessionRequest request) {
        MeetingSession session = MeetingSession.builder()
            .sessionName(request.getSessionName())
            .participantCount(request.getParticipantCount())
            .locations(request.getStartLocations())
            .preferences(request.getPreferences())
            .expiresAt(LocalDateTime.now().plusHours(24))
            .build();
            
        MeetingSession saved = sessionRepository.save(session);
        
        // Redis에 세션 캐시
        cacheSession(saved);
        
        return SessionResponse.from(saved);
    }
    
    /**
     * 세션 결과 저장
     */
    public void saveSessionResult(UUID sessionId, Object result) {
        MeetingSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));
            
        session.updateResult(result);
        session.updateLastAccessed();
        
        sessionRepository.save(session);
        cacheSession(session);
    }
    
    /**
     * 즐겨찾기 그룹으로 저장
     */
    public void saveAsUserGroup(UUID sessionId, String groupName) {
        MeetingSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));
            
        UserGroup group = UserGroup.builder()
            .groupName(groupName)
            .memberLocations(session.getLocations())
            .preferences(session.getPreferences())
            .build();
            
        groupRepository.save(group);
    }
    
    private void cacheSession(MeetingSession session) {
        String cacheKey = "session:" + session.getId();
        redisTemplate.opsForValue().set(cacheKey, session, Duration.ofHours(24));
    }
}
```

---

## 🌐 외부 API 연동 구조

### 지도 & 경로 서비스
```
Kakao API Services
├── Kakao Map API
│   ├── 지도 표시 (Frontend)
│   ├── 장소 검색 (Backend)
│   └── 좌표 변환 (Backend)
├── Kakao Mobility API  
│   ├── 대중교통 경로 (Backend)
│   ├── 소요시간 계산 (Backend)
│   └── 실시간 교통정보 (Backend)
└── Naver Direction API
    ├── 자동차 경로 (Backend)
    └── 도보 경로 (Backend)
```

### 부가 정보 서비스
```
Additional Services
├── OpenWeather API
│   ├── 현재 날씨 정보
│   ├── 시간별 예보
│   └── 미세먼지 정보
├── 공공데이터 API
│   ├── 문화시설 정보
│   └── 관광지 정보
└── 비즈니스 정보
    ├── 네이버 플레이스 (크롤링/API)
    └── 영업시간 정보
```

### API 클라이언트 구조
```java
// Kakao API 설정
@Configuration
public class ExternalApiConfig {
    
    @Value("${kakao.api.key}")
    private String kakaoApiKey;
    
    @Value("${naver.client.id}")
    private String naverClientId;
    
    @Value("${naver.client.secret}")  
    private String naverClientSecret;
    
    @Bean("kakaoWebClient")
    public WebClient kakaoWebClient() {
        return WebClient.builder()
            .baseUrl("https://dapi.kakao.com")
            .defaultHeader("Authorization", "KakaoAK " + kakaoApiKey)
            .defaultHeader("Content-Type", "application/json")
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
            .build();
    }
    
    @Bean("naverWebClient")
    public WebClient naverWebClient() {
        return WebClient.builder()
            .baseUrl("https://openapi.naver.com")
            .defaultHeader("X-Naver-Client-Id", naverClientId)
            .defaultHeader("X-Naver-Client-Secret", naverClientSecret)
            .build();
    }
    
    @Bean("weatherWebClient")  
    public WebClient weatherWebClient() {
        return WebClient.builder()
            .baseUrl("https://api.openweathermap.org/data/2.5")
            .build();
    }
}

// Kakao 지도 API 클라이언트
@Component
@RequiredArgsConstructor
public class KakaoMapApiClient {
    
    @Qualifier("kakaoWebClient")
    private final WebClient kakaoWebClient;
    
    /**
     * 주소를 좌표로 변환
     */
    @Cacheable(value = "address-to-coord", key = "#address")
    public Mono<CoordinateResponse> getCoordinateByAddress(String address) {
        return kakaoWebClient.get()
            .uri("/v2/local/search/address.json?query={address}", address)
            .retrieve()
            .bodyToMono(CoordinateResponse.class)
            .doOnError(error -> log.error("Kakao address search failed: {}", error.getMessage()));
    }
    
    /**
     * 주변 장소 검색
     */
    @Cacheable(value = "nearby-places", key = "#latitude + ':' + #longitude + ':' + #category")
    public Mono<PlaceSearchResponse> searchNearbyPlaces(
        double latitude, 
        double longitude, 
        String category,
        int radius
    ) {
        return kakaoWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/v2/local/search/category.json")
                .queryParam("category_group_code", category)
                .queryParam("x", longitude)
                .queryParam("y", latitude) 
                .queryParam("radius", radius)
                .queryParam("size", 15)
                .build())
            .retrieve()
            .bodyToMono(PlaceSearchResponse.class);
    }
}

// Kakao 모빌리티 API 클라이언트  
@Component
@RequiredArgsConstructor
public class KakaoMobilityApiClient {
    
    @Qualifier("kakaoWebClient")
    private final WebClient kakaoWebClient;
    
    /**
     * 대중교통 경로 조회
     */
    @Cacheable(value = "transit-routes", key = "#origin + ':' + #destination")
    public Mono<TransitRouteResponse> getTransitRoute(
        String origin, 
        String destination
    ) {
        return kakaoWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/v1/directions/transit")
                .queryParam("origin", origin)
                .queryParam("destination", destination)
                .build())
            .retrieve()
            .bodyToMono(TransitRouteResponse.class)
            .timeout(Duration.ofSeconds(5))
            .retryWhen(Retry.backoff(3, Duration.ofMillis(500)));
    }
}

// 날씨 API 클라이언트
@Component
@RequiredArgsConstructor
public class WeatherApiClient {
    
    @Qualifier("weatherWebClient")
    private final WebClient weatherWebClient;
    
    @Value("${openweather.api.key}")
    private String weatherApiKey;
    
    /**
     * 현재 날씨 정보 조회
     */
    @Cacheable(value = "weather", key = "#latitude + ':' + #longitude")
    public WeatherInfo getCurrentWeather(double latitude, double longitude) {
        try {
            WeatherResponse response = weatherWebClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/weather")
                    .queryParam("lat", latitude)
                    .queryParam("lon", longitude)
                    .queryParam("appid", weatherApiKey)
                    .queryParam("units", "metric")
                    .queryParam("lang", "kr")
                    .build())
                .retrieve()
                .bodyToMono(WeatherResponse.class)
                .timeout(Duration.ofSeconds(3))
                .block();
                
            return WeatherInfo.from(response);
        } catch (Exception e) {
            log.warn("Weather API call failed, using default weather info", e);
            return WeatherInfo.getDefault();
        }
    }
}
```

---

## 💾 데이터베이스 & 캐시 전략

### PostgreSQL 스키마 설계
```sql
-- 세션 관리 테이블
CREATE TABLE meeting_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_name VARCHAR(100) NOT NULL,
    participant_count INTEGER NOT NULL,
    start_locations JSONB NOT NULL,
    preferences JSONB,
    calculated_results JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP DEFAULT (CURRENT_TIMESTAMP + INTERVAL '24 hours'),
    last_accessed TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 장소 정보 캐시
CREATE TABLE places_cache (
    id SERIAL PRIMARY KEY,
    place_id VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    category VARCHAR(50),
    address TEXT,
    coordinates POINT,
    rating DECIMAL(2,1),
    price_range INTEGER,
    business_info JSONB,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스 생성
CREATE INDEX idx_places_coordinates ON places_cache USING GIST (coordinates);
CREATE INDEX idx_sessions_created_at ON meeting_sessions (created_at);
CREATE INDEX idx_places_category ON places_cache (category);
```

### Redis 캐싱 전략
```
캐시 구조
├── Route Cache
│   ├── Key: "route:{origin}:{destination}:{transport}"
│   ├── TTL: 30분
│   └── Value: RouteResponse JSON
├── Place Search Cache  
│   ├── Key: "places:{lat}:{lng}:{radius}:{category}"
│   ├── TTL: 1시간
│   └── Value: List<Place> JSON
└── Weather Cache
    ├── Key: "weather:{lat}:{lng}"
    ├── TTL: 10분
    └── Value: WeatherInfo JSON
```

---

## 🔒 보안 & 성능 고려사항

### 보안 설계
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .cors().and()
            .csrf().disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/sessions/**").authenticated()
                .anyRequest().authenticated())
            .build();
    }
}
```

### 성능 최적화
- **데이터베이스**: 연결 풀 최적화, 쿼리 튜닝
- **캐싱**: Redis 분산 캐시, 적절한 TTL 설정
- **API 호출**: 비동기 처리, 병렬 요청, 서킷 브레이커
- **프론트엔드**: 코드 스플리팅, 지연 로딩, 이미지 최적화

---

## 🚀 배포 아키텍처

### 개발/운영 환경
```
Development
├── Frontend: Vite Dev Server (localhost:5173)
├── Backend: Spring Boot (localhost:8080)
├── Database: Docker PostgreSQL
└── Cache: Docker Redis

Production  
├── Frontend: Nginx + Static Files
├── Backend: Spring Boot + Embedded Tomcat
├── Database: AWS RDS PostgreSQL
├── Cache: AWS ElastiCache Redis
└── Load Balancer: AWS ALB
```

### Docker 컨테이너화
```dockerfile
# Backend Dockerfile
FROM openjdk:17-jdk-slim
COPY build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]

# Frontend Dockerfile  
FROM nginx:alpine
COPY dist/ /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

---

## 🗄️ 데이터베이스 설계

### 주요 테이블 구조

```sql
-- 사용자 세션
CREATE TABLE meeting_sessions (
    id UUID PRIMARY KEY,
    session_name VARCHAR(100),
    participant_count INTEGER,
    locations JSONB,           -- 출발지 목록
    transportation JSONB,      -- 교통수단 선택
    preferences JSONB,         -- 예산, 목적, 필터
    result JSONB,             -- 추천 결과
    created_at TIMESTAMP DEFAULT NOW(),
    expires_at TIMESTAMP
);

-- 장소 정보 캐시
CREATE TABLE places_cache (
    id SERIAL PRIMARY KEY,
    place_id VARCHAR(100) UNIQUE,
    name VARCHAR(200) NOT NULL,
    category VARCHAR(50),
    address VARCHAR(300),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    rating DECIMAL(2, 1),
    price_range INTEGER,
    business_hours JSONB,
    contact_info JSONB,
    additional_data JSONB,
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 사용자 그룹 (즐겨찾기)
CREATE TABLE user_groups (
    id SERIAL PRIMARY KEY,
    group_name VARCHAR(100),
    member_locations JSONB,
    preferences JSONB,
    last_used TIMESTAMP DEFAULT NOW()
);
```

---

## 🔧 핵심 기능 상세 설계

### 1. 중간지점 계산 알고리즘

**계산 방식**:
1. **기하학적 중심점**: 위도/경도 좌표의 평균값
2. **이동시간 최적화**: 각 지점에서의 대중교통 소요시간 최소화
3. **가중치 적용**: 상권 발달도, 교통 접근성, 편의시설

**구현 로직**:
```java
@Service
public class MiddlePointCalculator {
    
    public List<OptimalLocation> calculateMeetingPoints(
        List<String> startLocations, 
        TransportType transportType
    ) {
        // 1. 각 출발지 좌표 변환
        // 2. 기하학적 중심점 계산
        // 3. 주변 교통 허브 탐색 (지하철역, 버스터미널)
        // 4. 이동시간 매트릭스 생성
        // 5. 최적화 알고리즘 적용
        // 6. 상위 5개 지점 반환
    }
}
```

### 2. 장소 추천 시스템

**추천 로직**:
- **카테고리별 가중치**: 목적에 따른 장소 유형 우선순위
- **거리 기반 점수**: 중간지점에서의 거리
- **인기도 점수**: 평점, 리뷰 수, 방문자 수
- **실용성 점수**: 영업시간, 예약 가능 여부, 가격대

### 3. 실시간 정보 연동

**날씨 기반 추천**:
```java
@Service
public class WeatherBasedRecommendation {
    
    @Autowired
    private WeatherService weatherService;
    
    public List<Place> getWeatherOptimizedPlaces(
        Location center, 
        PlaceCategory category
    ) {
        Weather currentWeather = weatherService.getCurrentWeather(center);
        
        if (currentWeather.isRainy()) {
            return getIndoorPlaces(center, category);
        } else if (currentWeather.getTemperature() > 30) {
            return getAirConditionedPlaces(center, category);
        }
        // ... 기타 날씨 조건
    }
}
```

---

## 📱 사용자 플로우

### 메인 플로우
1. **그룹 설정** → 인원 수, 그룹명 입력
2. **출발지 입력** → 각자의 출발 위치 설정
3. **옵션 선택** → 교통수단, 예산, 목적 선택
4. **중간지점 확인** → 추천된 만남 장소 검토
5. **세부 장소 선택** → 구체적인 카페/식당 선택
6. **결과 공유** → 카카오톡 등으로 일정 공유

### UI/UX 고려사항
- **모바일 퍼스트**: 반응형 디자인
- **직관적 지도 인터페이스**: 드래그 앤 드롭으로 위치 조정
- **단계별 진행**: 복잡한 설정을 단순화
- **실시간 미리보기**: 설정 변경 시 즉시 결과 업데이트

---

## 🚀 개발 일정 및 마일스톤

### Week 1-2: 프로젝트 셋업 & 기본 구조
- [ ] Spring Boot 프로젝트 초기화
- [ ] 데이터베이스 스키마 설계 및 구축
- [ ] 카카오 맵 API 연동 테스트
- [ ] 기본 REST API 엔드포인트 구현

### Week 3-4: 핵심 기능 구현
- [ ] 중간지점 계산 알고리즘 구현
- [ ] 장소 검색 및 필터링 기능
- [ ] 프론트엔드 기본 페이지 구현
- [ ] 지도 인터페이스 개발

### Week 5-6: 고도화 및 연동
- [ ] 실시간 정보 연동 (날씨, 영업시간)
- [ ] 사용자 세션 관리
- [ ] 결과 공유 기능
- [ ] 성능 최적화 및 캐싱

### Week 7-8: 테스트 & 배포
- [ ] 통합 테스트 및 버그 수정
- [ ] 사용성 테스트 (UX 개선)
- [ ] 서버 배포 및 모니터링 설정
- [ ] 베타 테스트 진행

---

## 📊 성공 지표 (KPI)

### 사용성 지표
- **완료율**: 검색 시작 → 장소 선택 완료까지 85% 이상
- **만족도**: 추천 장소의 실제 방문율 60% 이상
- **재사용률**: 30일 내 재방문율 40% 이상

### 기술 지표
- **응답시간**: API 응답시간 3초 이내
- **정확도**: 중간지점 계산 오차 500m 이내
- **가용성**: 99.5% 업타임 유지

### 비즈니스 지표
- **일간 활성 사용자**: 베타 기간 100명 목표
- **세션 지속시간**: 평균 5분 이상
- **추천 클릭률**: 제안 장소 클릭률 70% 이상

---

## 🔮 향후 확장 계획

### 추가 기능
- **AI 개인화**: 사용 패턴 학습을 통한 맞춤 추천
- **소셜 기능**: 리뷰 작성, 평점 시스템
- **예약 연동**: 네이버 예약, 캐치테이블 등 연동
- **비용 분할**: 더치페이 계산기 내장

### 비즈니스 모델
- **프리미엄 구독**: 고급 필터, 무제한 그룹 생성
- **제휴 수익**: 장소 예약 시 수수료
- **광고 모델**: 스폰서 장소 상위 노출

### 플랫폼 확장
- **모바일 앱**: React Native 또는 Flutter
- **카카오톡 미니앱**: 메신저 내 서비스 연동
- **기업용 버전**: 회사 워크숍, 팀 빌딩용

---

## 🛠️ 개발 태스크 분석 요청

Claude Code를 활용하여 다음 사항들을 분석해주세요:

1. **기술적 구현 복잡도 분석**
   - 각 기능별 개발 난이도 평가
   - 외부 API 연동 시 주의사항
   - 성능 병목 지점 예측

2. **우선순위 재조정**
   - MVP 기능의 중요도 순위
   - 개발 리소스 배분 최적화
   - 단계별 배포 전략

3. **리스크 분석**
   - 기술적 리스크 요소
   - 일정 지연 가능성
   - 대안 방안 제시

4. **코드 구조 설계**
   - 패키지 구조 최적화
   - 디자인 패턴 적용 방안
   - 테스트 전략 수립

이 기획서를 바탕으로 구체적인 개발 가이드라인을 제시해주시기 바랍니다.