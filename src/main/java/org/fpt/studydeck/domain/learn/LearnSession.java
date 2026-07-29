package org.fpt.studydeck.domain.learn;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.fpt.studydeck.domain.deck.Deck;
import org.fpt.studydeck.domain.deck.Flashcard;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "learn_sessions")
public class LearnSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LearnSessionStatus status;

    @Column(columnDefinition = "text")
    private String settingsJson;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant completedAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<LearnSessionItem> items = new ArrayList<>();

    protected LearnSession() {
    }

    public static LearnSession create(
            Deck deck,
            String settingsJson,
            List<Flashcard> flashcards,
            List<LearnQuestionType> questionTypes) {
        if (deck == null) {
            throw new IllegalArgumentException("Deck is required.");
        }

        LearnSession session = new LearnSession();
        session.deck = deck;
        session.status = LearnSessionStatus.ACTIVE;
        session.settingsJson = settingsJson;
        session.startedAt = Instant.now();
        session.updatedAt = session.startedAt;
        for (int position = 0; position < flashcards.size(); position++) {
            LearnQuestionType questionType = questionTypes.get(position % questionTypes.size());
            boolean trueFalseCorrectValue = true;
            String trueFalseWrongAnswer = null;
            if (questionType == LearnQuestionType.TRUE_FALSE && flashcards.size() > 1) {
                // Randomly decide if this should be a FALSE question (~50% chance)
                if (Math.random() < 0.5) {
                    trueFalseCorrectValue = false;
                    trueFalseWrongAnswer = pickRandomWrongAnswer(flashcards, position, PromptSide.TERM);
                }
            }
            session.addItem(flashcards.get(position), questionType, PromptSide.TERM, position, trueFalseCorrectValue,
                    trueFalseWrongAnswer);
        }
        return session;
    }

    private static String pickRandomWrongAnswer(List<Flashcard> flashcards, int currentIndex, PromptSide promptSide) {
        List<Integer> candidates = new ArrayList<>();
        for (int i = 0; i < flashcards.size(); i++) {
            if (i != currentIndex) {
                candidates.add(i);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        java.util.Collections.shuffle(candidates);
        Flashcard wrongCard = flashcards.get(candidates.get(0));
        return promptSide == PromptSide.TERM ? wrongCard.getDefinition() : wrongCard.getTerm();
    }

    private void addItem(Flashcard flashcard, LearnQuestionType questionType, PromptSide promptSide, int position,
            boolean trueFalseCorrectValue, String trueFalseWrongAnswer) {
        items.add(LearnSessionItem.create(this, flashcard, questionType, promptSide, position, trueFalseCorrectValue,
                trueFalseWrongAnswer));
    }

    public void complete() {
        this.status = LearnSessionStatus.COMPLETED;
        this.completedAt = Instant.now();
        this.updatedAt = this.completedAt;
    }

    void touch() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Deck getDeck() {
        return deck;
    }

    public LearnSessionStatus getStatus() {
        return status;
    }

    public String getSettingsJson() {
        return settingsJson;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<LearnSessionItem> getItems() {
        return items;
    }
}
