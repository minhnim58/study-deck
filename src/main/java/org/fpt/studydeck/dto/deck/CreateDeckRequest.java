package org.fpt.studydeck.dto.deck;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDeckRequest(
        Long folderId,

        @NotBlank(message = "Deck title is required.") @Size(max = 160) String title,

        @Size(max = 1000) String description,

        org.fpt.studydeck.domain.deck.Visibility visibility) {
}
