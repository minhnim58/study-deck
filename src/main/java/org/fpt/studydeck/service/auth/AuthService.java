package org.fpt.studydeck.service.auth;

import org.fpt.studydeck.domain.auth.AppUser;
import org.fpt.studydeck.dto.auth.AuthResponse;
import org.fpt.studydeck.dto.auth.AuthUserResponse;
import org.fpt.studydeck.exception.AuthenticationFailedException;
import org.fpt.studydeck.exception.ResourceConflictException;
import org.fpt.studydeck.repository.auth.AppUserRepository;
import org.fpt.studydeck.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final org.fpt.studydeck.repository.gamification.UserGamificationRepository userGamificationRepository;

    public AuthService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            org.fpt.studydeck.repository.gamification.UserGamificationRepository userGamificationRepository) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userGamificationRepository = userGamificationRepository;
    }

    @Transactional
    public AuthResponse register(String email, String password, String displayName) {
        String normalizedEmail = AppUser.normalizeEmail(email);
        if (appUserRepository.existsByEmail(normalizedEmail)) {
            throw new ResourceConflictException("Email is already registered.");
        }

        AppUser user = AppUser.create(
                normalizedEmail,
                passwordEncoder.encode(password),
                displayName);
        AppUser savedUser = appUserRepository.save(user);
        userGamificationRepository.save(org.fpt.studydeck.domain.gamification.UserGamification.create(savedUser));
        return buildAuthResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(String email, String password) {
        String normalizedEmail = AppUser.normalizeEmail(email);
        AppUser user = appUserRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new AuthenticationFailedException("Invalid email or password."));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new AuthenticationFailedException("Invalid email or password.");
        }

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(AppUser user) {
        return new AuthResponse(
                jwtService.generateToken(user.getEmail()),
                "Bearer",
                jwtService.getExpirationSeconds(),
                AuthUserResponse.from(user));
    }
}
