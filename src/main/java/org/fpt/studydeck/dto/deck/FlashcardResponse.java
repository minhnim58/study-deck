package org.fpt.studydeck.dto.deck;

import java.time.Instant;

import org.fpt.studydeck.domain.deck.Flashcard;

public record FlashcardResponse(
        Long id,
        Long deckId,
        String term,
        String definition,
        String termImageUrl,
        String definitionImageUrl,
        boolean starred,
        int position,
        Instant createdAt,
        Instant updatedAt) {

    public static FlashcardResponse from(Flashcard flashcard, String userEmail) {
        return new FlashcardResponse(
                flashcard.getId(),
                flashcard.getDeck().getId(),
                flashcard.getTerm(),
                flashcard.getDefinition(),
                flashcard.getTermImageUrl(),
                flashcard.getDefinitionImageUrl(),
                userEmail != null && flashcard.isStarredBy(userEmail),
                flashcard.getPosition(),
                flashcard.getCreatedAt(),
                flashcard.getUpdatedAt());
    }

    // Default method for legacy tests mapping
    public static FlashcardResponse from(Flashcard flashcard) {
        return from(flashcard, null);
    }
}
