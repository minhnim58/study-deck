package org.fpt.studydeck.dto.gamification;

public record DailyMissionStatusResponse(
    String missionKey,
    int progress,
    int target,
    boolean completed,
    boolean claimed
) {
}
