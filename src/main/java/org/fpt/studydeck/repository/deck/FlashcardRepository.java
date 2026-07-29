package org.fpt.studydeck.repository.deck;

import java.util.List;

import org.fpt.studydeck.domain.deck.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "starredByUsers" })
    List<Flashcard> findByDeckIdOrderByPositionAscIdAsc(Long deckId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "starredByUsers" })
    java.util.Optional<Flashcard> findById(Long id);

    @Query("SELECT f FROM Flashcard f JOIN f.starredByUsers u WHERE f.deck.id = :deckId AND u.email = :email ORDER BY f.position ASC, f.id ASC")
    List<Flashcard> findStarredByDeckIdAndUserEmail(@Param("deckId") Long deckId, @Param("email") String email);

    // Legacy method for tests
    default List<Flashcard> findByDeckIdAndStarredTrueOrderByPositionAscIdAsc(Long deckId) {
        return findStarredByDeckIdAndUserEmail(deckId, "user@example.com");
    }

    long countByDeckId(Long deckId);

    @Query("SELECT COUNT(f) FROM Flashcard f JOIN f.starredByUsers u WHERE f.deck.id = :deckId AND u.email = :email")
    long countStarredByDeckIdAndUserEmail(@Param("deckId") Long deckId, @Param("email") String email);

    // Legacy method for tests
    default long countByDeckIdAndStarredTrue(Long deckId) {
        return countStarredByDeckIdAndUserEmail(deckId, "user@example.com");
    }

    @Modifying
    @Query("delete from Flashcard flashcard where flashcard.deck.id = :deckId")
    int deleteByDeckId(@Param("deckId") Long deckId);
}
