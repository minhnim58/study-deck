package org.fpt.studydeck.service.srs;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fpt.studydeck.service.gamification.DailyMissionService;
import org.fpt.studydeck.service.gamification.GamificationService;

import org.fpt.studydeck.domain.deck.Flashcard;
import org.fpt.studydeck.domain.srs.SrsCardState;
import org.fpt.studydeck.domain.srs.SrsRating;
import org.fpt.studydeck.domain.srs.SrsReviewLog;
import org.fpt.studydeck.domain.srs.SrsState;
import org.fpt.studydeck.dto.srs.SrsCardStateResponse;
import org.fpt.studydeck.dto.srs.SrsDueCardResponse;
import org.fpt.studydeck.dto.srs.SrsReviewRequest;
import org.fpt.studydeck.dto.srs.SrsReviewResponse;
import org.fpt.studydeck.exception.InvalidRequestException;
import org.fpt.studydeck.exception.ResourceNotFoundException;
import org.fpt.studydeck.repository.deck.DeckRepository;
import org.fpt.studydeck.repository.deck.FlashcardRepository;
import org.fpt.studydeck.repository.srs.SrsCardStateRepository;
import org.fpt.studydeck.repository.srs.SrsReviewLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SrsReviewService {

    private static final String DECK_NOT_FOUND = "Deck was not found.";
    private static final String FLASHCARD_NOT_FOUND = "Flashcard was not found.";

    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;
    private final SrsCardStateRepository srsCardStateRepository;
    private final SrsReviewLogRepository srsReviewLogRepository;
    private final FsrsScheduler fsrsScheduler;
    private final GamificationService gamificationService;
    private final DailyMissionService dailyMissionService;

    public SrsReviewService(
        DeckRepository deckRepository,
        FlashcardRepository flashcardRepository,
        SrsCardStateRepository srsCardStateRepository,
        SrsReviewLogRepository srsReviewLogRepository,
        FsrsScheduler fsrsScheduler,
        GamificationService gamificationService,
        DailyMissionService dailyMissionService
    ) {
        this.deckRepository = deckRepository;
        this.flashcardRepository = flashcardRepository;
        this.srsCardStateRepository = srsCardStateRepository;
        this.srsReviewLogRepository = srsReviewLogRepository;
        this.fsrsScheduler = fsrsScheduler;
        this.gamificationService = gamificationService;
        this.dailyMissionService = dailyMissionService;
    }

    public SrsReviewResponse review(Long flashcardId, SrsReviewRequest request) {
        return review(flashcardId, request, null);
    }

    public SrsReviewResponse review(Long flashcardId, SrsReviewRequest request, String userEmail) {
        if (request.durationMs() < 0) {
            throw new InvalidRequestException("Duration must be zero or positive.");
        }

        Flashcard flashcard = flashcardRepository.findById(flashcardId)
            .orElseThrow(() -> new ResourceNotFoundException(FLASHCARD_NOT_FOUND));
        SrsCardState cardState = srsCardStateRepository.findByFlashcardId(flashcardId)
            .orElseGet(() -> SrsCardState.createNew(flashcard));

        SrsState previousState = cardState.getState();
        Instant dueBefore = cardState.getDueAt();
        Instant reviewedAt = Instant.now();
        FsrsScheduler.ScheduledReview scheduled = previousState == SrsState.NEW
            ? fsrsScheduler.reviewNewCard(request.rating(), reviewedAt)
            : fsrsScheduler.reviewExistingCard(cardState.getFsrsCardJson(), request.rating(), reviewedAt);
        int nextReps = cardState.getReps() + 1;
        int nextLapses = cardState.getLapses() + (request.rating() == SrsRating.AGAIN ? 1 : 0);

        cardState.applyReview(
            request.rating(),
            scheduled.state(),
            scheduled.dueAt(),
            scheduled.stability(),
            scheduled.difficulty(),
            scheduled.scheduledDays(),
            scheduled.elapsedDays(),
            nextReps,
            nextLapses,
            scheduled.fsrsCardJson(),
            reviewedAt
        );
        srsCardStateRepository.save(cardState);
        srsReviewLogRepository.save(SrsReviewLog.create(
            flashcard,
            request.rating(),
            reviewedAt,
            request.durationMs(),
            previousState,
            scheduled.state(),
            previousState == SrsState.NEW ? null : dueBefore,
            scheduled.dueAt(),
            null
        ));

        if (userEmail != null && !userEmail.isBlank()) {
            gamificationService.recordActivity(userEmail);
            dailyMissionService.updateMissionProgress(userEmail, "srs_reviewed_cards", 1, 10);
            gamificationService.awardPoints(userEmail, 2);
        }

        return new SrsReviewResponse(flashcardId, request.rating(), scheduled.state(), scheduled.dueAt(), nextReps, nextLapses);
    }

    @Transactional(readOnly = true)
    public List<SrsDueCardResponse> dueCards(Long deckId, Instant now) {
        if (!deckRepository.existsById(deckId)) {
            throw new ResourceNotFoundException(DECK_NOT_FOUND);
        }

        List<SrsCardState> existingDue = srsCardStateRepository.findDueByDeckId(deckId, now);
        Set<Long> existingDueIds = new HashSet<>();
        List<SrsDueCardResponse> dueExisting = existingDue.stream()
            .peek(state -> existingDueIds.add(state.getFlashcard().getId()))
            .map(state -> new SrsDueCardResponse(
                state.getFlashcard().getId(),
                state.getFlashcard().getTerm(),
                state.getFlashcard().getDefinition(),
                state.getDueAt(),
                state.getState()
            ))
            .toList();

        List<SrsDueCardResponse> newCards = flashcardRepository.findByDeckIdOrderByPositionAscIdAsc(deckId).stream()
            .filter(flashcard -> !srsCardStateRepository.findByFlashcardId(flashcard.getId()).isPresent())
            .map(flashcard -> new SrsDueCardResponse(
                flashcard.getId(),
                flashcard.getTerm(),
                flashcard.getDefinition(),
                now,
                SrsState.NEW
            ))
            .toList();

        return java.util.stream.Stream.concat(dueExisting.stream(), newCards.stream())
            .filter(card -> existingDueIds.contains(card.flashcardId()) || card.state() == SrsState.NEW)
            .toList();
    }

    @Transactional(readOnly = true)
    public SrsCardStateResponse getState(Long flashcardId) {
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
            .orElseThrow(() -> new ResourceNotFoundException(FLASHCARD_NOT_FOUND));
        return srsCardStateRepository.findByFlashcardId(flashcardId)
            .map(state -> new SrsCardStateResponse(
                flashcardId,
                state.getState(),
                state.getDueAt(),
                state.getStability(),
                state.getDifficulty(),
                state.getReps(),
                state.getLapses()
            ))
            .orElseGet(() -> new SrsCardStateResponse(flashcard.getId(), SrsState.NEW, Instant.EPOCH, 0, 0, 0, 0));
    }
}
