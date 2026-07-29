package org.fpt.studydeck.service.gamification;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.fpt.studydeck.domain.gamification.UserDailyMission;
import org.fpt.studydeck.domain.gamification.UserGamification;
import org.fpt.studydeck.dto.gamification.DailyMissionResponse;
import org.fpt.studydeck.exception.ResourceNotFoundException;
import org.fpt.studydeck.repository.gamification.UserDailyMissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DailyMissionService {

    private static final String MISSION_NOT_FOUND = "Daily mission was not found.";

    private final GamificationService gamificationService;
    private final UserDailyMissionRepository userDailyMissionRepository;

    public DailyMissionService(
        GamificationService gamificationService,
        UserDailyMissionRepository userDailyMissionRepository
    ) {
        this.gamificationService = gamificationService;
        this.userDailyMissionRepository = userDailyMissionRepository;
    }

    public List<DailyMissionResponse> getDailyMissions(String userEmail) {
        UserGamification gamification = gamificationService.getOrCreateForEmail(userEmail);
        LocalDate today = LocalDate.now();

        List<UserDailyMission> existingMissions = userDailyMissionRepository.findByGamificationAndMissionDate(gamification, today);
        return missionKeys().stream()
            .map(missionKey -> existingMissions.stream()
                .filter(mission -> mission.getMissionKey().equals(missionKey))
                .findFirst()
                .map(this::toResponse)
                .orElse(createDefaultMissionResponse(missionKey)))
            .toList();
    }

    public DailyMissionResponse claimDailyMission(String userEmail, String missionKey) {
        UserGamification gamification = gamificationService.getOrCreateForEmail(userEmail);
        LocalDate today = LocalDate.now();
        UserDailyMission mission = findOrCreateMission(gamification, missionKey, today);

        if (!mission.isCompleted() || mission.isClaimed()) {
            throw new IllegalStateException("Mission cannot be claimed.");
        }

        mission.setClaimed(true);
        userDailyMissionRepository.save(mission);
        gamificationService.awardPoints(gamification.getUser().getId(), getRewardForMission(missionKey));

        return toResponse(mission);
    }

    public UserDailyMission updateMissionProgress(String userEmail, String missionKey, int delta, int target) {
        UserGamification gamification = gamificationService.getOrCreateForEmail(userEmail);
        LocalDate today = LocalDate.now();

        UserDailyMission mission = findOrCreateMission(gamification, missionKey, today);
        mission.setProgress(Math.min(mission.getProgress() + delta, target));
        if (mission.getProgress() >= target) {
            mission.setCompleted(true);
        }

        return userDailyMissionRepository.save(mission);
    }

    private UserDailyMission findOrCreateMission(UserGamification gamification, String missionKey, LocalDate date) {
        return userDailyMissionRepository
            .findByGamificationAndMissionKeyAndMissionDate(gamification, missionKey, date)
            .orElseGet(() -> userDailyMissionRepository.save(UserDailyMission.create(gamification, missionKey, date)));
    }

    private List<String> missionKeys() {
        return List.of("practice_test_completed", "learn_session_completed", "srs_reviewed_cards");
    }

    private DailyMissionResponse createDefaultMissionResponse(String missionKey) {
        return new DailyMissionResponse(
            missionKey,
            getTitleForMission(missionKey),
            getDescriptionForMission(missionKey),
            0,
            getTargetForMission(missionKey),
            false,
            false,
            getRewardForMission(missionKey)
        );
    }

    private String getTitleForMission(String missionKey) {
        return switch (missionKey) {
            case "practice_test_completed" -> "Complete a practice test";
            case "learn_session_completed" -> "Finish a learn session";
            case "srs_reviewed_cards" -> "Review SRS flashcards";
            default -> missionKey.replace('_', ' ');
        };
    }

    private String getDescriptionForMission(String missionKey) {
        return switch (missionKey) {
            case "practice_test_completed" -> "Finish one practice test to earn a daily reward.";
            case "learn_session_completed" -> "Complete one learn session to keep your streak going.";
            case "srs_reviewed_cards" -> "Review ten SRS cards to earn bonus points.";
            default -> "Complete this task to earn reward points.";
        };
    }

    private long getRewardForMission(String missionKey) {
        return switch (missionKey) {
            case "practice_test_completed" -> 10L;
            case "learn_session_completed" -> 10L;
            case "srs_reviewed_cards" -> 10L;
            default -> 10L;
        };
    }

    private List<DailyMissionResponse> gamificationDailyMissions(UserGamification gamification, LocalDate date) {
        return userDailyMissionRepository.findByGamificationAndMissionDate(gamification, date).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    private DailyMissionResponse toResponse(UserDailyMission mission) {
        return new DailyMissionResponse(
            mission.getMissionKey(),
            getTitleForMission(mission.getMissionKey()),
            getDescriptionForMission(mission.getMissionKey()),
            mission.getProgress(),
            getTargetForMission(mission.getMissionKey()),
            mission.isCompleted(),
            mission.isClaimed(),
            getRewardForMission(mission.getMissionKey())
        );
    }

    private int getTargetForMission(String missionKey) {
        return switch (missionKey) {
            case "practice_test_completed" -> 1;
            case "learn_session_completed" -> 1;
            case "srs_reviewed_cards" -> 10;
            default -> 1;
        };
    }
}
