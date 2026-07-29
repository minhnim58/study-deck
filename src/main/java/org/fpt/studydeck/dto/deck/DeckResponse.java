package org.fpt.studydeck.dto.deck;

import java.time.Instant;

import org.fpt.studydeck.domain.deck.Deck;
import org.fpt.studydeck.domain.deck.DeckVisibility;

public record DeckResponse(
        Long id,
        Long folderId,
        String title,
        String description,
        DeckVisibility visibility,
        Instant createdAt,
        Instant updatedAt,
        int totalCards) {

    public static DeckResponse from(Deck deck) {
        Long folderId = deck.getFolder() == null ? null : deck.getFolder().getId();
        return new DeckResponse(
                deck.getId(),
                folderId,
                deck.getTitle(),
                deck.getDescription(),
                deck.getVisibility(),
                deck.getCreatedAt(),
                deck.getUpdatedAt(),
                deck.getTotalCards());
    }
}
