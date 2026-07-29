package org.fpt.studydeck.dto.gamification;

public record DailyMissionResponse(
    String missionKey,
    String title,
    String description,
    int progress,
    int target,
    boolean completed,
    boolean claimed,
    long rewardPoints
) {
}
