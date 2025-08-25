# 카카오맵 API 연동 가이드

## 1. 카카오 개발자 계정 생성

### 1-1. 카카오 디벨로퍼스 접속
- https://developers.kakao.com 접속
- 카카오 계정으로 로그인

### 1-2. 애플리케이션 생성
1. "내 애플리케이션" 메뉴 클릭
2. "애플리케이션 추가하기" 버튼 클릭
3. 앱 정보 입력:
   - **앱 이름**: LoadMapGuide Backend
   - **사업자명**: 개인 (또는 회사명)
   - **카테고리**: 지도/교통

### 1-3. 플랫폼 설정
1. 생성된 앱 → "플랫폼" 탭
2. "Web 플랫폼 등록" 클릭
3. 사이트 도메인 추가:
   - 개발: `http://localhost:8080`
   - 운영: 실제 도메인 (추후 추가)

### 1-4. API 키 확인
1. "요약 정보" 탭에서 확인:
   - **REST API 키**: `{발급받은 키}` (서버에서 사용)
   - **JavaScript 키**: `{발급받은 키}` (프론트에서 사용)

## 2. 필요한 API 서비스 활성화

### 2-1. 제품 설정
1. "제품 설정" → "카카오맵" 메뉴
2. 다음 API 활성화:
   - ✅ **주소 검색**: 주소 → 좌표 변환
   - ✅ **좌표-주소 변환**: 좌표 → 주소 변환  
   - ✅ **키워드 검색**: 장소명으로 검색
   - ✅ **카테고리 검색**: 카테고리별 장소 검색

### 2-2. 사용량 제한 확인
- **주소 검색**: 300,000건/일 (무료)
- **키워드 검색**: 100,000건/일 (무료)
- **카테고리 검색**: 100,000건/일 (무료)

## 3. 환경변수 설정

### 3-1. application-dev.yml 수정
```yaml
external:
  kakao:
    api-key: "${KAKAO_REST_API_KEY:your-rest-api-key-here}"
    base-url: "https://dapi.kakao.com"
    timeout: 5000
```

### 3-2. 환경변수 추가
```bash
# Windows (PowerShell)
$env:KAKAO_REST_API_KEY="your-actual-rest-api-key"

# Windows (CMD)  
set KAKAO_REST_API_KEY=your-actual-rest-api-key

# macOS/Linux
export KAKAO_REST_API_KEY="your-actual-rest-api-key"
```

### 3-3. IDE 환경변수 설정 (IntelliJ)
1. Run Configuration 편집
2. Environment Variables 섹션
3. `KAKAO_REST_API_KEY=실제발급받은키` 추가

## 4. API 사용 예시

### 4-1. 주소 → 좌표 변환
```bash
curl -X GET "https://dapi.kakao.com/v2/local/search/address.json?query=서울 강남구 역삼동" \
  -H "Authorization: KakaoAK your-rest-api-key"
```

### 4-2. 키워드 장소 검색
```bash
curl -X GET "https://dapi.kakao.com/v2/local/search/keyword.json?query=카페&x=127.037&y=37.5001" \
  -H "Authorization: KakaoAK your-rest-api-key"
```

## 5. 주의사항

1. **API 키 보안**: 환경변수로만 관리, 코드에 하드코딩 금지
2. **사용량 모니터링**: 개발자 콘솔에서 일일 사용량 확인
3. **에러 처리**: 429(제한 초과), 401(인증 실패) 응답 처리
4. **캐싱 전략**: 동일 요청 반복 방지를 위한 캐시 적용

---

## 다음 단계
1. ✅ API 키 발급 완료
2. 🔄 LocationCoordinateService 실제 API 연동
3. 🔄 PlaceSearchService 실제 API 연동
4. 🔄 통합 테스트 및 검증