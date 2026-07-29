package org.fpt.studydeck.domain.gamification;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_daily_missions")
public class UserDailyMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gamification_id", nullable = false)
    private UserGamification gamification;

    @Column(nullable = false, length = 100)
    private String missionKey;

    @Column(nullable = false)
    private LocalDate missionDate;

    @Column(nullable = false)
    private int progress;

    @Column(nullable = false)
    private boolean completed;

    @Column(nullable = false)
    private boolean claimed;

    @Column
    private Instant claimedAt;

    protected UserDailyMission() {
    }

    public static UserDailyMission create(UserGamification gamification, String missionKey, LocalDate missionDate) {
        UserDailyMission mission = new UserDailyMission();
        mission.gamification = gamification;
        mission.missionKey = missionKey;
        mission.missionDate = missionDate;
        mission.progress = 0;
        mission.completed = false;
        mission.claimed = false;
        mission.claimedAt = null;
        return mission;
    }

    public Long getId() {
        return id;
    }

    public UserGamification getGamification() {
        return gamification;
    }

    public String getMissionKey() {
        return missionKey;
    }

    public LocalDate getMissionDate() {
        return missionDate;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isClaimed() {
        return claimed;
    }

    public void setClaimed(boolean claimed) {
        this.claimed = claimed;
    }

    public Instant getClaimedAt() {
        return claimedAt;
    }

    public void setClaimedAt(Instant claimedAt) {
        this.claimedAt = claimedAt;
    }
}
