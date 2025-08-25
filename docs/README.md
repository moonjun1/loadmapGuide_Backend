# LoadMapGuide Backend 개발 문서

## 📋 프로젝트 개요
<img width="1400" height="921" alt="스크린샷 2025-08-24 232842" src="https://github.com/user-attachments/assets/cf430d78-e2c0-438a-9414-01851a5a2a48" />
<img width="669" height="837" alt="스크린샷 2025-08-24 232848" src="https://github.com/user-attachments/assets/ee8b2246-fde0-4aaf-aef3-53863df5aa05" />
<img width="777" height="786" alt="스크린샷 2025-08-25 133216" src="https://github.com/user-attachments/assets/d6cabb6c-cc06-4b5b-8f0d-cecfd4209a75" />

**중간지점 만남 서비스**의 백엔드 API 서버입니다. 여러 명이 만날 때 모두에게 공평한 중간지점을 찾고, 목적에 맞는 장소를 추천해주는 서비스입니다.

### 핵심 가치 제안
- **공평성**: 모든 참가자의 이동시간을 최소화하는 중간지점 계산
- **편의성**: 목적별 맞춤 장소 추천 (카페/놀이/공부)
- **실용성**: 실시간 정보 연동 (날씨, 영업시간, 예산)

---

## 🏗️ 기술 스택

### Backend
- **Framework**: Spring Boot 3.5.5
- **Language**: Java 17
- **Database**: H2 (개발) / PostgreSQL (운영 예정)
- **Cache**: Redis (운영) / Simple Cache (개발)
- **Build Tool**: Gradle 8.x

### 주요 의존성
- Spring Web, JPA, Validation, Cache, WebFlux
- Jackson, Lombok, SpringDoc OpenAPI
- H2, PostgreSQL, Redis

---

## 📁 프로젝트 구조

```
src/main/java/com/loadmapguide_backend/
├── domain/                          # 도메인별 비즈니스 로직
│   ├── location/                    # 위치 관련 도메인
│   │   ├── controller/              # REST API 엔드포인트
│   │   ├── service/                 # 비즈니스 로직
│   │   ├── repository/              # 데이터 액세스
│   │   ├── entity/                  # JPA 엔티티
│   │   └── dto/                     # 데이터 전송 객체
│   │
│   └── place/                       # 장소 관련 도메인
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── entity/
│       └── dto/
│
├── global/                          # 공통 기능
│   ├── config/                      # 설정 클래스
│   ├── exception/                   # 예외 처리
│   ├── external/                    # 외부 API 연동
│   └── common/                      # 공통 DTO, Enum
│
└── LoadmapGuideBackendApplication.java
```

---

## ✅ 개발 완료 현황

### 1단계: 프로젝트 초기 셋업 ✅
- [x] Spring Boot 프로젝트 구조 생성
- [x] 의존성 설정 및 build.gradle 구성
- [x] 환경별 설정 파일 (dev/prod)
- [x] 도메인 기반 패키지 구조 생성
- [x] 글로벌 예외 처리 및 설정 클래스

### 2단계: 핵심 기능 구현 ✅
- [x] **Entity 설계**: LocationPoint, Place, MeetingSession
- [x] **중간지점 계산 서비스**: 기하학적 중심점 + 격자 탐색 알고리즘
- [x] **장소 검색 서비스**: 위치 기반 필터링 및 정렬
- [x] **카카오맵 API 클라이언트**: 기본 구조 및 DTO
- [x] **REST API 컨트롤러**: 16개 엔드포인트 구현
- [x] **통합 테스트**: 핵심 API 동작 검증

---

## 🚀 구현된 주요 기능

### 중간지점 계산 알고리즘
- **알고리즘**: 기하학적 중심점 + 격자 패턴 후보 생성
- **점수 계산**: 이동시간(60%) + 상업지역 점수(40%)
- **교통수단 지원**: 대중교통, 자동차, 도보
- **결과**: 최적 중간지점 5개 후보 제시

### 장소 검색 및 필터링
- **위치 기반**: 중심점에서 반경 내 검색
- **다중 필터**: 카테고리, 예산, 평점, 키워드
- **정렬 옵션**: 거리순, 평점순, 인기순
- **페이징**: 결과 개수 제한

### API 엔드포인트 (16개)
```
# 위치 관련
POST /api/location/middle-point        # 세션 기반 중간지점 계산
POST /api/location/middle-point/simple # 간단 중간지점 계산
POST /api/location/validate           # 위치 유효성 검증

# 장소 관련  
POST /api/places/search               # 상세 장소 검색
GET  /api/places/nearby              # 주변 장소 검색
GET  /api/places/category/{category}  # 카테고리별 검색
GET  /api/places/{placeId}           # 장소 상세 조회
GET  /api/places/kakao/{kakaoId}     # 카카오 ID로 조회
GET  /api/places/categories          # 사용 가능 카테고리

# 기타
GET  /api/health                     # 헬스체크
GET  /api/actuator/health           # Actuator 헬스체크
```

---

## 📊 테스트 결과

### API 테스트 통과 현황
- ✅ Health Check API
- ✅ 중간지점 계산 API (3개 지점 → 5개 후보 반환)
- ✅ 장소 검색 API (빈 결과지만 로직 정상)
- ✅ 카테고리 조회 API (6개 카테고리 반환)
- ✅ 글로벌 예외 처리

### 성능 지표
- 중간지점 계산 시간: ~200ms (3개 지점 기준)
- 16개 API 엔드포인트 등록
- H2 데이터베이스 연동 성공
- JPA 엔티티 테이블 생성 확인

---

## 🎯 향후 개발 계획

### 3단계: 실제 API 연동 🔄 (진행 예정)
- [ ] **카카오 개발자 계정**: API 키 발급 및 설정
- [ ] **실제 지오코딩**: 주소 → 좌표 변환
- [ ] **실제 장소검색**: 카카오맵 장소 데이터 연동
- [ ] **환경변수 관리**: API 키 보안 설정
- [ ] **실제 데이터 테스트**: 서울 주요 지점 테스트

### 4단계: 고도화 기능
- [ ] **날씨 연동**: OpenWeather API 연동
- [ ] **샘플 데이터**: 서울 주요 카페/음식점 100개 추가
- [ ] **성능 최적화**: 알고리즘 개선, 캐싱 강화
- [ ] **Redis 캐시**: 실제 Redis 서버 연동

### 5단계: 프론트엔드 연동
- [ ] **React 프론트엔드**: 지도 인터페이스
- [ ] **카카오맵 연동**: 웹 지도 표시
- [ ] **사용자 인터페이스**: 출발지 입력, 결과 표시
- [ ] **반응형 디자인**: 모바일 지원

### 6단계: 운영 환경 구축
- [ ] **PostgreSQL**: 운영 DB 전환
- [ ] **Docker**: 컨테이너화
- [ ] **AWS/GCP 배포**: 클라우드 배포
- [ ] **CI/CD**: 자동 배포 파이프라인

---

## 🔧 개발 환경 설정

### 로컬 실행 방법
```bash
# 프로젝트 빌드
./gradlew clean build

# 개발 환경 실행
java -jar build/libs/loadmapGuide_Backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev

# 또는 Gradle로 실행
./gradlew bootRun
```

### 환경별 설정
- **개발환경**: H2 인메모리 DB, Simple Cache
- **운영환경**: PostgreSQL, Redis Cache

### API 테스트 예시
```bash
# 헬스체크
curl http://localhost:8080/api/health

# 중간지점 계산
curl -X POST http://localhost:8080/api/location/middle-point/simple \
  -H "Content-Type: application/json" \
  -d '[{"address":"서울 강남구","latitude":37.5001,"longitude":127.0374}]'

# 주변 장소 검색
curl "http://localhost:8080/api/places/nearby?latitude=37.545&longitude=126.981&radius=1000"
```

---

## 📚 참고 문서

- [기획서](../tesk/meeting_point_service_plan.md): 전체 서비스 기획 및 아키텍처
- [API 문서](http://localhost:8080/swagger-ui.html): Swagger UI (실행 시 접근 가능)
- [H2 콘솔](http://localhost:8080/h2-console): 개발 DB 콘솔 (실행 시 접근 가능)

---

## 🤝 개발 진행 상황

### 현재 상태
- ✅ **MVP 핵심 기능 완성**: 중간지점 계산 + 장소 검색
- 🔄 **실제 API 연동 진행중**: 카카오맵 API 연동 예정
- 📋 **다음 목표**: 실용적으로 사용 가능한 서비스 완성

### 개발 방식
- **도메인 주도 설계**: 비즈니스 로직별 모듈 분리
- **TDD 지향**: 테스트 우선 개발 (향후 강화 예정)
- **단계별 구현**: MVP → 고도화 → 운영화 순서

이 문서는 개발 진행에 따라 지속적으로 업데이트됩니다.
