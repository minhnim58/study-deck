package org.fpt.studydeck.dto.practice;

import java.util.List;

import org.fpt.studydeck.domain.learn.LearnQuestionType;
import org.fpt.studydeck.domain.learn.PromptSide;
import org.fpt.studydeck.domain.practice.PracticeTestQuestion;

public record PracticeQuestionResponse(
        Long id,
        Long flashcardId,
        LearnQuestionType questionType,
        PromptSide promptSide,
        String prompt,
        List<String> options,
        String submittedAnswer,
        Boolean correct) {

    public static PracticeQuestionResponse from(PracticeTestQuestion question) {
        return from(question, List.of());
    }

    public static PracticeQuestionResponse from(PracticeTestQuestion question, List<String> options) {
        var flashcard = question.getFlashcard();
        String prompt = question.getQuestionType() == LearnQuestionType.TRUE_FALSE
                ? flashcard.getTerm() + " = " + question.getCorrectAnswer()
                : prompt(question);
        List<String> effectiveOptions = List.of();
        if (question.getQuestionType() == LearnQuestionType.MULTIPLE_CHOICE) {
            effectiveOptions = options;
        } else if (question.getQuestionType() == LearnQuestionType.TRUE_FALSE) {
            effectiveOptions = List.of("True", "False");
        }
        return new PracticeQuestionResponse(
                question.getId(),
                flashcard.getId(),
                question.getQuestionType(),
                question.getPromptSide(),
                prompt,
                effectiveOptions,
                question.getSubmittedAnswer(),
                question.getCorrect());
    }

    private static String prompt(PracticeTestQuestion question) {
        return question.getPromptSide() == PromptSide.TERM
                ? question.getFlashcard().getTerm()
                : question.getFlashcard().getDefinition();
    }
}
