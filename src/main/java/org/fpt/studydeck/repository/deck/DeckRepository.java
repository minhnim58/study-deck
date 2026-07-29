package org.fpt.studydeck.repository.deck;

import java.util.List;

import org.fpt.studydeck.domain.deck.Deck;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;

public interface DeckRepository extends JpaRepository<Deck, Long> {

    @EntityGraph(attributePaths = { "creator" })
    List<Deck> findByFolderId(Long folderId);

    @EntityGraph(attributePaths = { "creator" })
    List<Deck> findAll();

    @EntityGraph(attributePaths = { "creator" })
    Optional<Deck> findById(Long id);
}
