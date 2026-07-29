package org.fpt.studydeck.repository.gamification;

import java.util.Optional;

import org.fpt.studydeck.domain.gamification.UserGamification;
import org.fpt.studydeck.domain.auth.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGamificationRepository extends JpaRepository<UserGamification, Long> {

    Optional<UserGamification> findByUser(AppUser user);
}
