package org.fpt.studydeck.dto.gamification;

import java.time.Instant;

import org.fpt.studydeck.domain.gamification.UserGamification;

public record UserGamificationResponse(
    long points,
    int level,
    int streakCount,
    Instant lastActiveAt,
    int nextLevelProgress,
    long nextLevelRequiredPoints
) {

    public static UserGamificationResponse from(UserGamification gamification) {
        long requiredPoints = gamification.getLevel() * 100L;
        int nextLevelProgress = requiredPoints <= 0
            ? 100
            : (int) Math.min(100, Math.round((double) gamification.getPoints() / requiredPoints * 100));

        return new UserGamificationResponse(
            gamification.getPoints(),
            gamification.getLevel(),
            gamification.getStreakCount(),
            gamification.getLastActiveAt(),
            nextLevelProgress,
            requiredPoints
        );
    }
}
