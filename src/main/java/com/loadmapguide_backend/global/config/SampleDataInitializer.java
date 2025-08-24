package com.loadmapguide_backend.global.config;

import com.loadmapguide_backend.domain.place.entity.Place;
import com.loadmapguide_backend.domain.place.repository.PlaceRepository;
import com.loadmapguide_backend.global.common.enums.PlaceCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("dev")
public class SampleDataInitializer implements CommandLineRunner {
    
    private final PlaceRepository placeRepository;
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (placeRepository.count() > 0) {
            log.info("샘플 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }
        
        log.info("샘플 데이터 초기화를 시작합니다...");
        initializeSamplePlaces();
        log.info("샘플 데이터 초기화가 완료되었습니다. 총 {}개 장소가 생성되었습니다.", 
                placeRepository.count());
    }
    
    private void initializeSamplePlaces() {
        List<Place> samplePlaces = List.of(
            // 강남 지역 카페
            createPlace("스타벅스 강남역점", PlaceCategory.CAFE, 37.498095, 127.027610, 
                    "서울 강남구 강남대로 390", 15000, 4.2, "강남역 2번출구 바로 앞"),
            createPlace("투썸플레이스 강남점", PlaceCategory.CAFE, 37.497175, 127.025892, 
                    "서울 강남구 테헤란로 113", 18000, 4.1, "조용하고 넓은 카페"),
            createPlace("블루보틀 강남점", PlaceCategory.CAFE, 37.499707, 127.026291, 
                    "서울 강남구 테헤란로 152", 25000, 4.5, "프리미엄 스페셜티 커피"),
            
            // 강남 지역 음식점
            createPlace("본죽 강남역점", PlaceCategory.RESTAURANT, 37.497536, 127.028451, 
                    "서울 강남구 강남대로 390", 12000, 4.0, "건강한 죽 전문점"),
            createPlace("맥도날드 강남역점", PlaceCategory.RESTAURANT, 37.498372, 127.027033, 
                    "서울 강남구 강남대로 396", 8000, 3.8, "24시간 패스트푸드"),
            createPlace("한솥도시락 강남점", PlaceCategory.RESTAURANT, 37.498123, 127.027891, 
                    "서울 강남구 테헤란로 108", 6000, 3.9, "저렴한 한식 도시락"),
            
            // 홍대 지역 카페
            createPlace("엔제리너스 홍대점", PlaceCategory.CAFE, 37.551931, 126.922700, 
                    "서울 마포구 와우산로 94", 16000, 4.0, "홍대 메인스트리트 위치"),
            createPlace("커피빈 홍대역점", PlaceCategory.CAFE, 37.556881, 126.924734, 
                    "서울 마포구 어울마당로 55", 17000, 3.9, "홍대입구역 근처"),
            createPlace("할리스 홍대점", PlaceCategory.CAFE, 37.553142, 126.921033, 
                    "서울 마포구 독막로 98", 14000, 4.1, "넓고 조용한 공간"),
            
            // 홍대 지역 음식점
            createPlace("교촌치킨 홍대점", PlaceCategory.RESTAURANT, 37.554539, 126.923456, 
                    "서울 마포구 와우산로 119", 20000, 4.3, "유명 치킨 프랜차이즈"),
            createPlace("버거킹 홍대점", PlaceCategory.RESTAURANT, 37.556234, 126.925123, 
                    "서울 마포구 어울마당로 88", 9000, 3.7, "햄버거 전문점"),
            
            // 명동 지역 카페
            createPlace("스타벅스 명동역점", PlaceCategory.CAFE, 37.560963, 126.986107, 
                    "서울 중구 명동길 52", 16000, 4.2, "명동 중심가 위치"),
            createPlace("파스쿠찌 명동점", PlaceCategory.CAFE, 37.563692, 126.983780, 
                    "서울 중구 명동10길 29", 18000, 4.0, "이탈리안 커피 전문점"),
            
            // 명동 지역 음식점
            createPlace("명동교자 본점", PlaceCategory.RESTAURANT, 37.563126, 126.983345, 
                    "서울 중구 명동10길 29", 15000, 4.4, "전통 만두 맛집"),
            createPlace("구름떡볶이 명동점", PlaceCategory.RESTAURANT, 37.562384, 126.985672, 
                    "서울 중구 명동8나길 16", 8000, 4.1, "유명 떡볶이 체인"),
            
            // 스터디 카페
            createPlace("스터디카페 더스테이지", PlaceCategory.STUDY_CAFE, 37.499234, 127.029876, 
                    "서울 강남구 테헤란로 123", 3000, 4.3, "24시간 스터디카페"),
            createPlace("이디야 스터디카페", PlaceCategory.STUDY_CAFE, 37.554721, 126.920834, 
                    "서울 마포구 와우산로 77", 2500, 4.0, "조용한 스터디 공간"),
            
            // 문화시설
            createPlace("CGV 강남점", PlaceCategory.ENTERTAINMENT, 37.498756, 127.027234, 
                    "서울 강남구 강남대로 438", 12000, 4.1, "최신 영화 상영관"),
            createPlace("홍대 라이브클럽", PlaceCategory.ENTERTAINMENT, 37.553987, 126.922456, 
                    "서울 마포구 와우산로 21길", 20000, 4.2, "인디 음악 라이브"),
            
            // 쇼핑
            createPlace("강남 지하상가", PlaceCategory.SHOPPING, 37.497845, 127.027123, 
                    "서울 강남구 강남대로 지하", 0, 3.8, "다양한 쇼핑몰"),
            createPlace("명동 쇼핑거리", PlaceCategory.SHOPPING, 37.563456, 126.984234, 
                    "서울 중구 명동길 일대", 0, 4.3, "패션 쇼핑의 메카")
        );
        
        placeRepository.saveAll(samplePlaces);
    }
    
    private Place createPlace(String name, PlaceCategory category, double latitude, double longitude,
                             String address, int avgPrice, double rating, String description) {
        return Place.builder()
                .name(name)
                .category(category)
                .latitude(latitude)
                .longitude(longitude)
                .address(address)
                .priceRange(avgPrice / 5000) // 5000원 단위로 가격대 설정
                .rating(rating)
                .build();
    }
}