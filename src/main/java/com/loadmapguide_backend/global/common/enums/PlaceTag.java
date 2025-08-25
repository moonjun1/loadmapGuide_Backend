package com.loadmapguide_backend.global.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 장소 태그 enum
 * 사용자가 원하는 목적에 따른 장소 분류
 */
@Getter
@RequiredArgsConstructor
public enum PlaceTag {
    
    // 📚 공부/학습 관련
    QUIET_STUDY("조용한 공부", "조용하고 집중하기 좋은 환경", "📚"),
    STUDY_CAFE("스터디카페", "전용 스터디 공간", "📖"),
    WIFI_GOOD("와이파이 좋음", "인터넷 연결이 안정적인 곳", "📶"),
    POWER_OUTLET("콘센트 많음", "충전하기 편한 곳", "🔌"),
    OPEN_24H("24시간 운영", "늦은 시간까지 이용 가능", "🕐"),
    LIBRARY("도서관/독서실", "공공 학습 공간", "📚"),
    
    // 🍽️ 먹기 좋은 곳
    TASTY_FOOD("맛집", "맛있기로 유명한 곳", "😋"),
    GOOD_VALUE("가성비 좋음", "가격 대비 만족도 높은 곳", "💰"),
    NICE_ATMOSPHERE("분위기 좋음", "인테리어나 분위기가 특별한 곳", "✨"),
    GROUP_FRIENDLY("단체 가능", "여러 명이 함께 이용하기 좋은 곳", "👥"),
    RESERVATION_NEEDED("예약 필수", "미리 예약이 필요한 곳", "📞"),
    LATE_NIGHT("야식/늦은 시간", "늦은 시간에도 운영하는 곳", "🌙"),
    
    // 🎮 놀기 좋은 곳
    KARAOKE("노래방", "노래를 부를 수 있는 곳", "🎤"),
    BOWLING("볼링장", "볼링을 칠 수 있는 곳", "🎳"),
    CINEMA("영화관", "영화를 볼 수 있는 곳", "🎬"),
    ARCADE("오락실/게임", "게임을 즐길 수 있는 곳", "🕹️"),
    BOARD_GAME("보드게임", "보드게임을 즐길 수 있는 곳", "🎲"),
    ESCAPE_ROOM("방탈출", "방탈출 게임을 할 수 있는 곳", "🔐"),
    
    // ☕ 모임/대화
    CONVERSATION("대화하기 좋음", "조용히 대화하기 좋은 환경", "💬"),
    SPACIOUS("넓은 공간", "여유롭고 넓은 좌석", "🏢"),
    PARKING("주차 가능", "주차가 편리한 곳", "🅿️"),
    NICE_VIEW("뷰 좋음", "전망이나 풍경이 좋은 곳", "🌆"),
    OUTDOOR("야외 공간", "테라스나 야외 좌석이 있는 곳", "🌳"),
    
    // 🚇 접근성
    SUBWAY_NEAR("지하철 근처", "지하철역에서 가까운 곳", "🚇"),
    BUS_NEAR("버스 정류장 근처", "버스 이용이 편리한 곳", "🚌"),
    WALK_ACCESSIBLE("도보 접근 좋음", "걸어서 가기 편한 곳", "🚶"),
    
    // 💸 가격대
    BUDGET_FRIENDLY("저렴함", "학생들도 부담 없는 가격", "💸"),
    MID_RANGE("적당한 가격", "중간 정도의 가격대", "💳"),
    PREMIUM("고급스러움", "특별한 날을 위한 곳", "💎");
    
    private final String displayName;
    private final String description;
    private final String emoji;
    
    /**
     * 카테고리별 태그 그룹핑
     */
    public static PlaceTag[] getStudyTags() {
        return new PlaceTag[]{QUIET_STUDY, STUDY_CAFE, WIFI_GOOD, POWER_OUTLET, OPEN_24H, LIBRARY};
    }
    
    public static PlaceTag[] getFoodTags() {
        return new PlaceTag[]{TASTY_FOOD, GOOD_VALUE, NICE_ATMOSPHERE, GROUP_FRIENDLY, RESERVATION_NEEDED, LATE_NIGHT};
    }
    
    public static PlaceTag[] getEntertainmentTags() {
        return new PlaceTag[]{KARAOKE, BOWLING, CINEMA, ARCADE, BOARD_GAME, ESCAPE_ROOM};
    }
    
    public static PlaceTag[] getMeetingTags() {
        return new PlaceTag[]{CONVERSATION, SPACIOUS, PARKING, NICE_VIEW, OUTDOOR};
    }
    
    public static PlaceTag[] getAccessibilityTags() {
        return new PlaceTag[]{SUBWAY_NEAR, BUS_NEAR, WALK_ACCESSIBLE};
    }
    
    public static PlaceTag[] getPriceTags() {
        return new PlaceTag[]{BUDGET_FRIENDLY, MID_RANGE, PREMIUM};
    }
}