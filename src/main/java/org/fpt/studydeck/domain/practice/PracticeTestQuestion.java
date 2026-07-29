package org.fpt.studydeck.domain.practice;

import java.time.Instant;

import org.fpt.studydeck.domain.deck.Flashcard;
import org.fpt.studydeck.domain.learn.LearnQuestionType;
import org.fpt.studydeck.domain.learn.PromptSide;

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
@Table(name = "practice_test_questions")
public class PracticeTestQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "practice_test_id", nullable = false)
    private PracticeTest practiceTest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flashcard_id", nullable = false)
    private Flashcard flashcard;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LearnQuestionType questionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PromptSide promptSide;

    @Column(nullable = false, length = 1000)
    private String correctAnswer;

    @Column(length = 1000)
    private String submittedAnswer;

    private Boolean correct;

    @Column(length = 1000)
    private String trueFalseWrongAnswer;

    private Instant answeredAt;

    @Column(nullable = false)
    private int position;

    protected PracticeTestQuestion() {
    }

    public static PracticeTestQuestion create(
            PracticeTest practiceTest,
            Flashcard flashcard,
            LearnQuestionType questionType,
            PromptSide promptSide,
            int position) {
        return create(practiceTest, flashcard, questionType, promptSide, position, null);
    }

    public static PracticeTestQuestion create(
            PracticeTest practiceTest,
            Flashcard flashcard,
            LearnQuestionType questionType,
            PromptSide promptSide,
            int position,
            String trueFalseWrongAnswer) {
        if (flashcard == null) {
            throw new IllegalArgumentException("Flashcard is required.");
        }

        PracticeTestQuestion question = new PracticeTestQuestion();
        question.practiceTest = practiceTest;
        question.flashcard = flashcard;
        question.questionType = questionType;
        question.promptSide = promptSide;
        question.position = position;

        if (questionType == LearnQuestionType.TRUE_FALSE) {
            if (trueFalseWrongAnswer != null) {
                // This is a FALSE question - shown definition is wrong
                question.correctAnswer = "false";
            } else {
                // This is a TRUE question - shown definition is correct
                question.correctAnswer = "true";
            }
            question.trueFalseWrongAnswer = trueFalseWrongAnswer;
        } else {
            question.correctAnswer = promptSide == PromptSide.TERM
                    ? flashcard.getDefinition()
                    : flashcard.getTerm();
        }

        return question;
    }

    public void answer(String submittedAnswer, boolean correct) {
        this.submittedAnswer = submittedAnswer;
        this.correct = correct;
        this.answeredAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public PracticeTest getPracticeTest() {
        return practiceTest;
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

    public String getTrueFalseWrongAnswer() {
        return trueFalseWrongAnswer;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public String getSubmittedAnswer() {
        return submittedAnswer;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public Instant getAnsweredAt() {
        return answeredAt;
    }

    public int getPosition() {
        return position;
    }
}
