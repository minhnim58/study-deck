package org.fpt.studydeck.dto.deck;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFolderRequest(
        @NotBlank(message = "Folder name is required.") @Size(max = 120) String name,

        @Size(max = 1000) String description,

        org.fpt.studydeck.domain.deck.Visibility visibility) {
}
