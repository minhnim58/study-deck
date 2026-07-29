package org.fpt.studydeck.dto.deck;

import org.fpt.studydeck.domain.deck.Flashcard;

public record ViewerCardResponse(
        Long id,
        String term,
        String definition,
        String termImageUrl,
        String definitionImageUrl,
        boolean starred,
        int position) {

    public static ViewerCardResponse from(Flashcard flashcard, String userEmail) {
        return new ViewerCardResponse(
                flashcard.getId(),
                flashcard.getTerm(),
                flashcard.getDefinition(),
                flashcard.getTermImageUrl(),
                flashcard.getDefinitionImageUrl(),
                userEmail != null && flashcard.isStarredBy(userEmail),
                flashcard.getPosition());
    }

    // Legacy support
    public static ViewerCardResponse from(Flashcard flashcard) {
        return from(flashcard, null);
    }
}
