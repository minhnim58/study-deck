package org.fpt.studydeck.dto.learn;

import java.util.List;

import org.fpt.studydeck.domain.learn.LearnQuestionType;
import org.fpt.studydeck.domain.learn.LearnSessionItem;
import org.fpt.studydeck.domain.learn.PromptSide;

public record LearnSessionItemResponse(
        Long id,
        Long flashcardId,
        LearnQuestionType questionType,
        PromptSide promptSide,
        String prompt,
        String answer,
        List<String> options,
        int attempts) {

    public static LearnSessionItemResponse from(LearnSessionItem item) {
        return from(item, List.of());
    }

    public static LearnSessionItemResponse from(LearnSessionItem item, List<String> options) {
        var flashcard = item.getFlashcard();
        if (item.getQuestionType() == LearnQuestionType.TRUE_FALSE) {
            String shownDefinition = item.getPromptSide() == PromptSide.TERM
                    ? flashcard.getDefinition()
                    : flashcard.getTerm();
            // correctAnswer stored as "true" or "false" in the session item
            // For true/false, prompt shows "term = someDefinition"
            // We need to get the actual shown answer from the item's stored data
            return new LearnSessionItemResponse(
                    item.getId(),
                    flashcard.getId(),
                    item.getQuestionType(),
                    item.getPromptSide(),
                    flashcard.getTerm() + " = " + shownDefinition,
                    "true",
                    List.of("True", "False"),
                    item.getAttempts());
        }

        boolean promptWithTerm = item.getPromptSide() == PromptSide.TERM;
        List<String> effectiveOptions = item.getQuestionType() == LearnQuestionType.MULTIPLE_CHOICE
                ? options
                : List.of();
        return new LearnSessionItemResponse(
                item.getId(),
                flashcard.getId(),
                item.getQuestionType(),
                item.getPromptSide(),
                promptWithTerm ? flashcard.getTerm() : flashcard.getDefinition(),
                promptWithTerm ? flashcard.getDefinition() : flashcard.getTerm(),
                effectiveOptions,
                item.getAttempts());
    }
}
