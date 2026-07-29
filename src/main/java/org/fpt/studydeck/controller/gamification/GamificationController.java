package org.fpt.studydeck.controller.gamification;

import java.security.Principal;
import java.util.List;

import org.fpt.studydeck.dto.gamification.DailyMissionResponse;
import org.fpt.studydeck.dto.gamification.UserGamificationResponse;
import org.fpt.studydeck.service.gamification.DailyMissionService;
import org.fpt.studydeck.service.gamification.GamificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me")
public class GamificationController {

    private final GamificationService gamificationService;
    private final DailyMissionService dailyMissionService;

    public GamificationController(
        GamificationService gamificationService,
        DailyMissionService dailyMissionService
    ) {
        this.gamificationService = gamificationService;
        this.dailyMissionService = dailyMissionService;
    }

    @GetMapping("/gamification")
    public UserGamificationResponse getGamification(Principal principal) {
        return gamificationService.getStats(principal.getName());
    }

    @GetMapping("/daily-missions")
    public List<DailyMissionResponse> getDailyMissions(Principal principal) {
        return dailyMissionService.getDailyMissions(principal.getName());
    }

    @PostMapping("/daily-missions/{missionKey}/claim")
    public DailyMissionResponse claimDailyMission(
        @PathVariable String missionKey,
        Principal principal
    ) {
        return dailyMissionService.claimDailyMission(principal.getName(), missionKey);
    }
}
