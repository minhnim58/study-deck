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
        Boolean correct,
        String correctAnswer) {

    public static PracticeQuestionResponse from(PracticeTestQuestion question) {
        return from(question, List.of());
    }

    public static PracticeQuestionResponse from(PracticeTestQuestion question, List<String> options) {
        var flashcard = question.getFlashcard();
        String prompt;
        if (question.getQuestionType() == LearnQuestionType.TRUE_FALSE) {
            String candidateAnswer;
            if (question.getTrueFalseWrongAnswer() != null) {
                candidateAnswer = question.getTrueFalseWrongAnswer();
            } else {
                candidateAnswer = question.getPromptSide() == PromptSide.TERM
                        ? flashcard.getDefinition()
                        : flashcard.getTerm();
            }
            String promptBase = question.getPromptSide() == PromptSide.TERM
                    ? flashcard.getTerm()
                    : flashcard.getDefinition();
            prompt = promptBase + " = " + candidateAnswer;
        } else {
            prompt = prompt(question);
        }
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
                question.getCorrect(),
                question.getCorrectAnswer());
    }

    private static String prompt(PracticeTestQuestion question) {
        return question.getPromptSide() == PromptSide.TERM
                ? question.getFlashcard().getTerm()
                : question.getFlashcard().getDefinition();
    }
}
