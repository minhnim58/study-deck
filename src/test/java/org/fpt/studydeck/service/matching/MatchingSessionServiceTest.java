package org.fpt.studydeck.service.matching;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.fpt.studydeck.domain.matching.MatchingSessionStatus;
import org.fpt.studydeck.dto.matching.CreateMatchingSessionRequest;
import org.fpt.studydeck.dto.matching.MatchingAnswerRequest;
import org.fpt.studydeck.exception.InvalidRequestException;
import org.fpt.studydeck.exception.ResourceConflictException;
import org.fpt.studydeck.exception.ResourceNotFoundException;
import org.fpt.studydeck.repository.matching.MatchingSessionRepository;
import org.fpt.studydeck.service.deck.DeckService;
import org.fpt.studydeck.service.deck.FlashcardService;
import org.fpt.studydeck.service.deck.ViewerCardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import jakarta.persistence.EntityManager;

import org.fpt.studydeck.service.gamification.DailyMissionService;
import org.fpt.studydeck.service.gamification.GamificationService;

@DataJpaTest
@Import({ DeckService.class, FlashcardService.class, ViewerCardService.class, GamificationService.class,
        DailyMissionService.class, MatchingSessionService.class })
class MatchingSessionServiceTest {

    @Autowired
    private DeckService deckService;

    @Autowired
    private FlashcardService flashcardService;

    @Autowired
    private MatchingSessionService matchingSessionService;

    @Autowired
    private MatchingSessionRepository matchingSessionRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void createSessionWithTenCardsReturnsTenItemsAndActiveStatus() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        createCards(deck.getId(), 10);

        var session = matchingSessionService.createSession(
                deck.getId(),
                new CreateMatchingSessionRequest(10, false));

        assertThat(session.status()).isEqualTo(MatchingSessionStatus.ACTIVE.name());
        assertThat(session.cardCount()).isEqualTo(10);
        assertThat(session.items()).hasSize(10);
        assertThat(session.matchedCount()).isZero();
    }

    @Test
    void matchOneItemIncrementsMatchedCountToOne() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        createCards(deck.getId(), 2);
        var session = matchingSessionService.createSession(deck.getId(), new CreateMatchingSessionRequest(2, false));

        var matched = matchingSessionService.match(
                session.id(),
                new MatchingAnswerRequest(session.items().get(0).id()));

        assertThat(matched.matchedCount()).isEqualTo(1);
        assertThat(matched.status()).isEqualTo(MatchingSessionStatus.ACTIVE.name());
    }

    @Test
    void matchingAllItemsCompletesSessionAndSetsDuration() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        createCards(deck.getId(), 2);
        var session = matchingSessionService.createSession(deck.getId(), new CreateMatchingSessionRequest(2, false));

        var first = matchingSessionService.match(session.id(), new MatchingAnswerRequest(session.items().get(0).id()));
        var completed = matchingSessionService.match(session.id(),
                new MatchingAnswerRequest(first.items().get(1).id()));

        assertThat(completed.status()).isEqualTo(MatchingSessionStatus.COMPLETED.name());
        assertThat(completed.durationMs()).isGreaterThanOrEqualTo(0);
        assertThat(matchingSessionRepository.findById(session.id()))
                .get()
                .satisfies(stored -> {
                    assertThat(stored.getCompletedAt()).isNotNull();
                    assertThat(stored.getDurationMs()).isGreaterThanOrEqualTo(0);
                });
    }

    @Test
    void starredOnlyWithInsufficientStarredCardsThrowsInvalidRequestException() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        var first = flashcardService.createFlashcard(deck.getId(), "term 1", "definition 1", null, null);
        flashcardService.createFlashcard(deck.getId(), "term 2", "definition 2", null, null);
        flashcardService.setStarred(first.getId(), true);

        assertThatThrownBy(() -> matchingSessionService.createSession(
                deck.getId(),
                new CreateMatchingSessionRequest(2, true)))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("Not enough cards are available for matching.");
    }

    @Test
    void completedSessionRejectsFurtherMatches() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        createCards(deck.getId(), 1);
        var session = matchingSessionService.createSession(deck.getId(), new CreateMatchingSessionRequest(1, false));
        var completed = matchingSessionService.match(
                session.id(),
                new MatchingAnswerRequest(session.items().get(0).id()));

        assertThatThrownBy(() -> matchingSessionService.match(
                session.id(),
                new MatchingAnswerRequest(completed.items().get(0).id())))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("Matching session is already completed.");
    }

    @Test
    void getSessionMissingThrowsResourceNotFoundException() {
        assertThatThrownBy(() -> matchingSessionService.getSession(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Matching session was not found.");
    }

    @Test
    void matchMissingItemThrowsResourceNotFoundException() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        createCards(deck.getId(), 1);
        var session = matchingSessionService.createSession(deck.getId(), new CreateMatchingSessionRequest(1, false));

        assertThatThrownBy(() -> matchingSessionService.match(session.id(), new MatchingAnswerRequest(999L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Matching session item was not found.");
    }

    @Test
    void matchItemFromAnotherSessionThrowsResourceNotFoundException() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        createCards(deck.getId(), 2);
        var firstSession = matchingSessionService.createSession(deck.getId(),
                new CreateMatchingSessionRequest(1, false));
        var secondSession = matchingSessionService.createSession(deck.getId(),
                new CreateMatchingSessionRequest(1, false));

        assertThatThrownBy(() -> matchingSessionService.match(
                firstSession.id(),
                new MatchingAnswerRequest(secondSession.items().get(0).id())))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Matching session item was not found.");
    }

    @Test
    void matchCompletesWhenPersistedStateHasNoUnmatchedItemsDespiteStaleLoadedCollection() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        createCards(deck.getId(), 2);
        var session = matchingSessionService.createSession(deck.getId(), new CreateMatchingSessionRequest(2, false));
        var firstItemId = session.items().get(0).id();
        var secondItemId = session.items().get(1).id();

        entityManager.flush();
        var staleSession = matchingSessionRepository.findById(session.id()).orElseThrow();
        assertThat(staleSession.getItems()).hasSize(2);

        entityManager.createQuery("""
                update MatchingSessionItem item
                set item.matched = true, item.matchedAt = :matchedAt
                where item.id = :itemId
                """)
                .setParameter("matchedAt", Instant.now())
                .setParameter("itemId", secondItemId)
                .executeUpdate();

        var completed = matchingSessionService.match(session.id(), new MatchingAnswerRequest(firstItemId));

        assertThat(completed.status()).isEqualTo(MatchingSessionStatus.COMPLETED.name());
        assertThat(completed.durationMs()).isGreaterThanOrEqualTo(0);
    }

    private void createCards(Long deckId, int count) {
        for (int index = 1; index <= count; index++) {
            flashcardService.createFlashcard(deckId, "term " + index, "definition " + index, null, null);
        }
    }
}
