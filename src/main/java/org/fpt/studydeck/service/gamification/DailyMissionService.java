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
        UserDailyMission mission = userDailyMissionRepository
            .findByGamificationAndMissionKeyAndMissionDate(gamification, missionKey, today)
            .orElseThrow(() -> new ResourceNotFoundException(MISSION_NOT_FOUND));

        if (!mission.isCompleted() || mission.isClaimed()) {
            throw new IllegalStateException("Mission cannot be claimed.");
        }

        mission.setClaimed(true);
        userDailyMissionRepository.save(mission);
        gamificationService.awardPoints(gamification.getUser().getId(), 10);

        return toResponse(mission);
    }

    public UserDailyMission updateMissionProgress(String userEmail, String missionKey, int delta, int target) {
        UserGamification gamification = gamificationService.getOrCreateForEmail(userEmail);
        LocalDate today = LocalDate.now();

        UserDailyMission mission = userDailyMissionRepository
            .findByGamificationAndMissionKeyAndMissionDate(gamification, missionKey, today)
            .orElseGet(() -> userDailyMissionRepository.save(UserDailyMission.create(gamification, missionKey, today)));

        mission.setProgress(Math.min(mission.getProgress() + delta, target));
        if (mission.getProgress() >= target) {
            mission.setCompleted(true);
        }

        return userDailyMissionRepository.save(mission);
    }

    private List<String> missionKeys() {
        return List.of("practice_test_completed", "learn_session_completed", "srs_reviewed_cards");
    }

    private DailyMissionResponse createDefaultMissionResponse(String missionKey) {
        return new DailyMissionResponse(
            missionKey,
            missionKey.replace('_', ' '),
            "Complete this task to earn reward points.",
            0,
            getTargetForMission(missionKey),
            false,
            false,
            10L
        );
    }

    private List<DailyMissionResponse> gamificationDailyMissions(UserGamification gamification, LocalDate date) {
        return userDailyMissionRepository.findByGamificationAndMissionDate(gamification, date).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    private DailyMissionResponse toResponse(UserDailyMission mission) {
        return new DailyMissionResponse(
            mission.getMissionKey(),
            mission.getMissionKey().replace('_', ' '),
            "Complete this task to earn reward points.",
            mission.getProgress(),
            getTargetForMission(mission.getMissionKey()),
            mission.isCompleted(),
            mission.isClaimed(),
            10L
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
