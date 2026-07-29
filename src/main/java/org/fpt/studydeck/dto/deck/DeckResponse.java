package org.fpt.studydeck.dto.deck;

import java.time.Instant;

import org.fpt.studydeck.domain.deck.Deck;
import org.fpt.studydeck.domain.deck.Visibility;

public record DeckResponse(
        Long id,
        Long folderId,
        String title,
        String description,
        Visibility visibility,
        Long creatorId,
        String creatorDisplayName,
        Instant createdAt,
        Instant updatedAt,
        int totalCards) {

    public static DeckResponse from(Deck deck) {
        Long folderId = deck.getFolder() == null ? null : deck.getFolder().getId();
        Long creatorId = deck.getCreator() == null ? null : deck.getCreator().getId();
        String creatorDisplayName = deck.getCreator() == null ? null : deck.getCreator().getDisplayName();

        return new DeckResponse(
                deck.getId(),
                folderId,
                deck.getTitle(),
                deck.getDescription(),
                deck.getVisibility(),
                creatorId,
                creatorDisplayName,
                deck.getCreatedAt(),
                deck.getUpdatedAt(),
                deck.getTotalCards());
    }
}
