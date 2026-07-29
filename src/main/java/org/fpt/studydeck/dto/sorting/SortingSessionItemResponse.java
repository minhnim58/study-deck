package org.fpt.studydeck.dto.sorting;

import org.fpt.studydeck.domain.sorting.SortingAnswer;
import org.fpt.studydeck.domain.sorting.SortingSessionItem;
import org.fpt.studydeck.domain.deck.Flashcard;

public record SortingSessionItemResponse(
        Long id,
        Long flashcardId,
        String term,
        String definition,
        String termImageUrl,
        String definitionImageUrl,
        boolean starred,
        int position,
        SortingAnswer answer) {

    public static SortingSessionItemResponse from(SortingSessionItem sessionItem, String userEmail) {
        Flashcard flashcard = sessionItem.getFlashcard();
        return new SortingSessionItemResponse(
                sessionItem.getId(),
                flashcard.getId(),
                flashcard.getTerm(),
                flashcard.getDefinition(),
                flashcard.getTermImageUrl(),
                flashcard.getDefinitionImageUrl(),
                userEmail != null && flashcard.isStarredBy(userEmail),
                sessionItem.getPosition(),
                sessionItem.getAnswer());
    }

    // Legacy support
    public static SortingSessionItemResponse from(SortingSessionItem sessionItem) {
        return from(sessionItem, null);
    }
}
