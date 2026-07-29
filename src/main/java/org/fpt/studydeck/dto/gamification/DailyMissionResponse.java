package org.fpt.studydeck.dto.gamification;

import java.time.Instant;

public record DailyMissionResponse(
    String key,
    String title,
    String description,
    int target,
    int progress,
    long rewardPoints,
    String status,
    Instant claimedAt
) {
}
