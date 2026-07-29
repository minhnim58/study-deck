package org.fpt.studydeck.domain.gamification;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import org.fpt.studydeck.domain.auth.AppUser;

@Entity
@Table(name = "user_gamification")
public class UserGamification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    @Column(nullable = false)
    private long points;

    @Column(nullable = false)
    private int level;

    @Column(nullable = false)
    private int streakCount;

    @Column
    private Instant lastActiveAt;

    @Column
    private LocalDate lastMissionDate;

    protected UserGamification() {
    }

    public static UserGamification create(AppUser user) {
        UserGamification gamification = new UserGamification();
        gamification.user = user;
        gamification.points = 0L;
        gamification.level = 1;
        gamification.streakCount = 0;
        gamification.lastActiveAt = null;
        gamification.lastMissionDate = null;
        return gamification;
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public long getPoints() {
        return points;
    }

    public void setPoints(long points) {
        this.points = points;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getStreakCount() {
        return streakCount;
    }

    public void setStreakCount(int streakCount) {
        this.streakCount = streakCount;
    }

    public Instant getLastActiveAt() {
        return lastActiveAt;
    }

    public void setLastActiveAt(Instant lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }

    public LocalDate getLastMissionDate() {
        return lastMissionDate;
    }

    public void setLastMissionDate(LocalDate lastMissionDate) {
        this.lastMissionDate = lastMissionDate;
    }
}
