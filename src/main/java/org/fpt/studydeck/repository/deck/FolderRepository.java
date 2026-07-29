package org.fpt.studydeck.repository.deck;

import org.fpt.studydeck.domain.deck.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;
import java.util.List;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    @EntityGraph(attributePaths = { "creator" })
    List<Folder> findAll();

    @EntityGraph(attributePaths = { "creator" })
    Optional<Folder> findById(Long id);
}
