package org.fpt.studydeck.service.deck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.fpt.studydeck.domain.srs.SrsRating;
import org.fpt.studydeck.dto.learn.CreateLearnSessionRequest;
import org.fpt.studydeck.dto.srs.SrsReviewRequest;
import org.fpt.studydeck.repository.srs.SrsCardStateRepository;
import org.fpt.studydeck.repository.srs.SrsReviewLogRepository;
import org.fpt.studydeck.service.srs.FsrsScheduler;
import org.fpt.studydeck.service.srs.SrsReviewService;
import org.fpt.studydeck.service.learn.LearnSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({ DeckService.class, FlashcardService.class, FsrsScheduler.class, SrsReviewService.class,
        LearnSessionService.class })
class FlashcardServiceTest {

    @Autowired
    private DeckService deckService;

    @Autowired
    private FlashcardService flashcardService;

    @Autowired
    private SrsReviewService srsReviewService;

    @Autowired
    private LearnSessionService learnSessionService;

    @Autowired
    private org.fpt.studydeck.repository.auth.AppUserRepository appUserRepository;

    @Autowired
    private org.fpt.studydeck.repository.deck.FlashcardRepository flashcardRepository;

    @Autowired
    private SrsCardStateRepository srsCardStateRepository;

    @Autowired
    private SrsReviewLogRepository srsReviewLogRepository;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        if (!appUserRepository.existsByEmail("user@example.com")) {
            org.fpt.studydeck.domain.auth.AppUser user = org.fpt.studydeck.domain.auth.AppUser
                    .create("user@example.com", "password", "Test User");
            appUserRepository.save(user);
        }
    }

    @Test
    void createsFlashcardWithOptionalImageUrls() {
        var deck = deckService.createDeck(null, "Korean Basics", null);

        var flashcard = flashcardService.createFlashcard(
                deck.getId(),
                " 현장 ",
                " site ",
                " https://example.com/term.png ",
                " https://example.com/definition.png ");

        assertThat(flashcard.getTerm()).isEqualTo("현장");
        assertThat(flashcard.getDefinition()).isEqualTo("site");
        assertThat(flashcard.getTermImageUrl()).isEqualTo("https://example.com/term.png");
        assertThat(flashcard.getDefinitionImageUrl()).isEqualTo("https://example.com/definition.png");
        assertThat(flashcard.isStarredBy("user@example.com")).isFalse();
        assertThat(flashcard.getPosition()).isZero();
    }

    @Test
    void togglesStarredFlag() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        var flashcard = flashcardService.createFlashcard(deck.getId(), "현장", "site", null, null);

        flashcardService.setStarred(flashcard.getId(), "user@example.com", true);
        assertThat(flashcardRepository.findById(flashcard.getId()).get().isStarredBy("user@example.com")).isTrue();

        flashcardService.setStarred(flashcard.getId(), "user@example.com", false);
        assertThat(flashcardRepository.findById(flashcard.getId()).get().isStarredBy("user@example.com")).isFalse();
    }

    @Test
    void rejectsBlankTerm() {
        var deck = deckService.createDeck(null, "Korean Basics", null);

        assertThatThrownBy(() -> flashcardService.createFlashcard(deck.getId(), " ", "site", null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Term is required.");
    }

    @Test
    void deletesReviewedFlashcardAndSrsData() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        var flashcard = flashcardService.createFlashcard(deck.getId(), "현장", "site", null, null);
        srsReviewService.review(flashcard.getId(), new SrsReviewRequest(SrsRating.GOOD, 1200));

        flashcardService.deleteFlashcard(flashcard.getId());

        assertThatThrownBy(() -> flashcardService.getFlashcard(flashcard.getId()))
                .isInstanceOf(org.fpt.studydeck.exception.ResourceNotFoundException.class)
                .hasMessage("Flashcard was not found.");
        assertThat(srsCardStateRepository.findByFlashcardId(flashcard.getId())).isEmpty();
        assertThat(srsReviewLogRepository.count()).isZero();
    }

    @Test
    void deletesFlashcardReferencedByLearnSession() {
        var deck = deckService.createDeck(null, "Korean Basics", null);
        var flashcard = flashcardService.createFlashcard(deck.getId(), "현장", "site", null, null);
        learnSessionService.createSession(
                deck.getId(),
                new CreateLearnSessionRequest(0, true, false, false, false, false, false));

        flashcardService.deleteFlashcard(flashcard.getId());

        assertThatThrownBy(() -> flashcardService.getFlashcard(flashcard.getId()))
                .isInstanceOf(org.fpt.studydeck.exception.ResourceNotFoundException.class)
                .hasMessage("Flashcard was not found.");
    }
}
