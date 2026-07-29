package org.fpt.studydeck.dto.gamification;

import java.time.Instant;

public record DailyMissionStatusResponse(
    String missionKey,
    String status,
    int progress,
    int target,
    Instant claimedAt
) {
}
