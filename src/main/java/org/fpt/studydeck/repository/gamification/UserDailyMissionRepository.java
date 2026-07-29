package org.fpt.studydeck.repository.gamification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.fpt.studydeck.domain.gamification.UserDailyMission;
import org.fpt.studydeck.domain.gamification.UserGamification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDailyMissionRepository extends JpaRepository<UserDailyMission, Long> {

    List<UserDailyMission> findByGamificationAndMissionDate(UserGamification gamification, LocalDate missionDate);

    Optional<UserDailyMission> findByGamificationAndMissionKeyAndMissionDate(UserGamification gamification, String missionKey, LocalDate missionDate);
}
