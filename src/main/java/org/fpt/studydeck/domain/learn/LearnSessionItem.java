package org.fpt.studydeck.domain.learn;

import java.time.Instant;

import org.fpt.studydeck.domain.deck.Flashcard;

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
import jakarta.persistence.Table;

@Entity
@Table(name = "learn_session_items")
public class LearnSessionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private LearnSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LearnQuestionType questionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PromptSide promptSide;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LearnItemStatus status;

    @Column(nullable = false)
    private int attempts;

    @Column(nullable = false)
    private int correctCount;

    @Column(nullable = false)
    private int wrongCount;

    private Instant lastAnsweredAt;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private boolean trueFalseCorrectValue = true;

    protected LearnSessionItem() {
    }

    public static LearnSessionItem create(
            LearnSession session,
            Flashcard flashcard,
            LearnQuestionType questionType,
            PromptSide promptSide,
            int position) {
        return create(session, flashcard, questionType, promptSide, position, true);
    }

    public static LearnSessionItem create(
            LearnSession session,
            Flashcard flashcard,
            LearnQuestionType questionType,
            PromptSide promptSide,
            int position,
            boolean trueFalseCorrectValue) {
        if (flashcard == null) {
            throw new IllegalArgumentException("Flashcard is required.");
        }

        LearnSessionItem item = new LearnSessionItem();
        item.session = session;
        item.flashcard = flashcard;
        item.questionType = questionType;
        item.promptSide = promptSide;
        item.status = LearnItemStatus.ACTIVE;
        item.position = position;
        item.trueFalseCorrectValue = trueFalseCorrectValue;
        return item;
    }

    public void answer(boolean correct) {
        attempts++;
        if (correct) {
            correctCount++;
            status = LearnItemStatus.CORRECT;
        } else {
            wrongCount++;
            status = LearnItemStatus.WRONG;
        }
        lastAnsweredAt = Instant.now();
        session.touch();
    }

    public boolean isTrueFalseCorrectValue() {
        return trueFalseCorrectValue;
    }

    public Long getId() {
        return id;
    }

    public LearnSession getSession() {
        return session;
    }

    public Flashcard getFlashcard() {
        return flashcard;
    }

    public LearnQuestionType getQuestionType() {
        return questionType;
    }

    public PromptSide getPromptSide() {
        return promptSide;
    }

    public LearnItemStatus getStatus() {
        return status;
    }

    public int getAttempts() {
        return attempts;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public int getWrongCount() {
        return wrongCount;
    }

    public Instant getLastAnsweredAt() {
        return lastAnsweredAt;
    }

    public int getPosition() {
        return position;
    }
}
