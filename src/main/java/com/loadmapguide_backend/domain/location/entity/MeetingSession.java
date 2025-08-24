package com.loadmapguide_backend.domain.location.entity;

import com.loadmapguide_backend.global.common.entity.BaseEntity;
import com.loadmapguide_backend.global.common.enums.TransportationType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "meeting_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingSession extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "session_name", nullable = false, length = 100)
    private String sessionName;
    
    @Column(name = "participant_count", nullable = false)
    private Integer participantCount;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "start_locations", nullable = false, columnDefinition = "json")
    private List<LocationPoint> startLocations;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transportation_type")
    private TransportationType transportationType;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferences", columnDefinition = "json")
    private PreferenceSettings preferences;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "calculated_results", columnDefinition = "json")
    private Object calculatedResults;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;
    
    @Builder
    public MeetingSession(String sessionName, Integer participantCount, 
                         List<LocationPoint> startLocations, TransportationType transportationType,
                         PreferenceSettings preferences) {
        this.sessionName = sessionName;
        this.participantCount = participantCount;
        this.startLocations = startLocations;
        this.transportationType = transportationType;
        this.preferences = preferences;
        this.expiresAt = LocalDateTime.now().plusHours(24);
        this.lastAccessed = LocalDateTime.now();
    }
    
    public void updateResults(Object results) {
        this.calculatedResults = results;
        this.lastAccessed = LocalDateTime.now();
    }
    
    public void updateLastAccessed() {
        this.lastAccessed = LocalDateTime.now();
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}