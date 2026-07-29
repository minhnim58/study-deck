package org.fpt.studydeck.service.gamification;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.fpt.studydeck.domain.auth.AppUser;
import org.fpt.studydeck.domain.gamification.UserGamification;
import org.fpt.studydeck.dto.gamification.UserGamificationResponse;
import org.fpt.studydeck.exception.ResourceNotFoundException;
import org.fpt.studydeck.repository.auth.AppUserRepository;
import org.fpt.studydeck.repository.gamification.UserGamificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GamificationService {

    private static final String USER_NOT_FOUND = "User was not found.";

    private final AppUserRepository appUserRepository;
    private final UserGamificationRepository userGamificationRepository;

    public GamificationService(
            AppUserRepository appUserRepository,
            UserGamificationRepository userGamificationRepository) {
        this.appUserRepository = appUserRepository;
        this.userGamificationRepository = userGamificationRepository;
    }

    public UserGamification getOrCreateForEmail(String userEmail) {
        AppUser user = getUserByEmail(userEmail);
        return userGamificationRepository.findByUser(user)
                .orElseGet(() -> {
                    try {
                        return userGamificationRepository.save(UserGamification.create(user));
                    } catch (org.springframework.dao.DataIntegrityViolationException e) {
                        return userGamificationRepository.findByUser(user)
                                .orElseThrow(() -> new IllegalStateException(
                                        "Failed to retrieve or create UserGamification"));
                    }
                });
    }

    public UserGamification awardPoints(String userEmail, long amount) {
        UserGamification gamification = getOrCreateForEmail(userEmail);
        return awardPoints(gamification.getUser().getId(), amount);
    }

    public UserGamificationResponse getStats(String userEmail) {
        UserGamification gamification = getOrCreateForEmail(userEmail);
        return UserGamificationResponse.from(gamification);
    }

    public UserGamification awardPoints(Long userId, long amount) {
        UserGamification gamification = getOrCreateForUser(userId);
        gamification.setPoints(gamification.getPoints() + amount);
        updateLevel(gamification);
        return userGamificationRepository.save(gamification);
    }

    public UserGamification recordActivity(String userEmail) {
        UserGamification gamification = getOrCreateForEmail(userEmail);
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate lastActiveDate = gamification.getLastActiveAt() == null
                ? null
                : gamification.getLastActiveAt().atZone(ZoneId.systemDefault()).toLocalDate();

        if (!today.equals(lastActiveDate)) {
            if (lastActiveDate != null && lastActiveDate.plusDays(1).equals(today)) {
                gamification.setStreakCount(gamification.getStreakCount() + 1);
            } else {
                gamification.setStreakCount(1);
            }
            gamification.setLastActiveAt(Instant.now());
            return userGamificationRepository.save(gamification);
        }

        return gamification;
    }

    private UserGamification getOrCreateForUser(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
        return userGamificationRepository.findByUser(user)
                .orElseGet(() -> {
                    try {
                        return userGamificationRepository.save(UserGamification.create(user));
                    } catch (org.springframework.dao.DataIntegrityViolationException e) {
                        return userGamificationRepository.findByUser(user)
                                .orElseThrow(() -> new IllegalStateException(
                                        "Failed to retrieve or create UserGamification"));
                    }
                });
    }

    private AppUser getUserByEmail(String userEmail) {
        return appUserRepository.findByEmail(AppUser.normalizeEmail(userEmail))
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
    }

    private void updateLevel(UserGamification gamification) {
        long requiredPoints = pointsNeededForNextLevel(gamification.getLevel());
        if (gamification.getPoints() >= requiredPoints) {
            gamification.setLevel(gamification.getLevel() + 1);
        }
    }

    private long pointsNeededForNextLevel(int level) {
        return level * 100L;
    }
}
