package com.loadmapguide_backend.domain.place.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class BusinessHours {
    
    private List<DaySchedule> weeklySchedule;
    private String holidayInfo;
    private String specialNote;
    
    @Builder
    public BusinessHours(List<DaySchedule> weeklySchedule, String holidayInfo, String specialNote) {
        this.weeklySchedule = weeklySchedule;
        this.holidayInfo = holidayInfo;
        this.specialNote = specialNote;
    }
    
    @Getter
    @NoArgsConstructor
    public static class DaySchedule {
        private String dayOfWeek;
        private LocalTime openTime;
        private LocalTime closeTime;
        private boolean isOpen;
        private String breakTime;
        
        @Builder
        public DaySchedule(String dayOfWeek, LocalTime openTime, LocalTime closeTime,
                          boolean isOpen, String breakTime) {
            this.dayOfWeek = dayOfWeek;
            this.openTime = openTime;
            this.closeTime = closeTime;
            this.isOpen = isOpen;
            this.breakTime = breakTime;
        }
    }
}