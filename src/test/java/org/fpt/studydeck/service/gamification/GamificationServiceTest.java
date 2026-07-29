package org.fpt.studydeck.service.gamification;

import static org.assertj.core.api.Assertions.assertThat;

import org.fpt.studydeck.domain.auth.AppUser;
import org.fpt.studydeck.repository.auth.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({GamificationService.class, DailyMissionService.class})
class GamificationServiceTest {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private GamificationService gamificationService;

    @Autowired
    private DailyMissionService dailyMissionService;

    @Test
    void getStatsIncludesNextLevelProgressAndRequiredPoints() {
        AppUser user = appUserRepository.save(AppUser.create("student@example.com", "hashed", "Student"));

        gamificationService.awardPoints(user.getId(), 75);

        var stats = gamificationService.getStats(user.getEmail());

        assertThat(stats.points()).isEqualTo(75);
        assertThat(stats.nextLevelProgress()).isEqualTo(75);
        assertThat(stats.nextLevelRequiredPoints()).isEqualTo(100L);
    }

    @Test
    void dailyMissionsExposeProgressStatusAndClaimedAt() {
        AppUser user = appUserRepository.save(AppUser.create("student@example.com", "hashed", "Student"));

        var missions = dailyMissionService.getDailyMissions(user.getEmail());
        var practiceMission = missions.stream()
            .filter(mission -> "practice_test_completed".equals(mission.key()))
            .findFirst()
            .orElseThrow();

        assertThat(practiceMission.status()).isEqualTo("IN_PROGRESS");

        dailyMissionService.updateMissionProgress(user.getEmail(), "practice_test_completed", 1, 1);
        var updated = dailyMissionService.getDailyMissions(user.getEmail()).stream()
            .filter(mission -> "practice_test_completed".equals(mission.key()))
            .findFirst()
            .orElseThrow();

        assertThat(updated.status()).isEqualTo("COMPLETED");

        dailyMissionService.claimDailyMission(user.getEmail(), "practice_test_completed");
        var claimed = dailyMissionService.getDailyMissions(user.getEmail()).stream()
            .filter(mission -> "practice_test_completed".equals(mission.key()))
            .findFirst()
            .orElseThrow();

        assertThat(claimed.status()).isEqualTo("CLAIMED");
        assertThat(claimed.claimedAt()).isNotNull();
    }
}
