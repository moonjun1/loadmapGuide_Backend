package com.loadmapguide_backend.domain.location.repository;

import com.loadmapguide_backend.domain.location.entity.MeetingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MeetingSessionRepository extends JpaRepository<MeetingSession, UUID> {
    
    List<MeetingSession> findBySessionNameContaining(String sessionName);
    
    List<MeetingSession> findByParticipantCount(Integer participantCount);
    
    @Query("SELECT ms FROM MeetingSession ms WHERE ms.expiresAt > :currentTime")
    List<MeetingSession> findActiveSessions(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT ms FROM MeetingSession ms WHERE ms.expiresAt <= :currentTime")
    List<MeetingSession> findExpiredSessions(@Param("currentTime") LocalDateTime currentTime);
    
    @Modifying
    @Query("DELETE FROM MeetingSession ms WHERE ms.expiresAt <= :currentTime")
    int deleteExpiredSessions(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT COUNT(ms) FROM MeetingSession ms WHERE " +
           "ms.createdAt >= :startDate AND ms.createdAt < :endDate")
    long countSessionsCreatedBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}