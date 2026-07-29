package org.fpt.studydeck.dto.deck;

import java.time.Instant;

import org.fpt.studydeck.domain.deck.Folder;
import org.fpt.studydeck.domain.deck.Visibility;

public record FolderResponse(
        Long id,
        String name,
        String description,
        Visibility visibility,
        Long creatorId,
        String creatorDisplayName,
        int position,
        Instant createdAt,
        Instant updatedAt) {

    public static FolderResponse from(Folder folder) {
        Long creatorId = folder.getCreator() == null ? null : folder.getCreator().getId();
        String creatorDisplayName = folder.getCreator() == null ? null : folder.getCreator().getDisplayName();

        return new FolderResponse(
                folder.getId(),
                folder.getName(),
                folder.getDescription(),
                folder.getVisibility(),
                creatorId,
                creatorDisplayName,
                folder.getPosition(),
                folder.getCreatedAt(),
                folder.getUpdatedAt());
    }
}
