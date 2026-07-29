package org.fpt.studydeck.dto.gamification;

import java.time.Instant;

import org.fpt.studydeck.domain.gamification.UserGamification;

public record UserGamificationResponse(
    long points,
    int level,
    int streakCount,
    Instant lastActiveAt
) {

    public static UserGamificationResponse from(UserGamification gamification) {
        return new UserGamificationResponse(
            gamification.getPoints(),
            gamification.getLevel(),
            gamification.getStreakCount(),
            gamification.getLastActiveAt()
        );
    }
}
