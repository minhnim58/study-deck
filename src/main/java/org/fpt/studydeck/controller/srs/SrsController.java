package org.fpt.studydeck.controller.srs;

import java.security.Principal;
import java.time.Instant;
import java.util.List;

import jakarta.validation.Valid;
import org.fpt.studydeck.dto.srs.SrsCardStateResponse;
import org.fpt.studydeck.dto.srs.SrsDueCardResponse;
import org.fpt.studydeck.dto.srs.SrsReviewRequest;
import org.fpt.studydeck.dto.srs.SrsReviewResponse;
import org.fpt.studydeck.dto.srs.SrsStatsResponse;
import org.fpt.studydeck.exception.InvalidRequestException;
import org.fpt.studydeck.service.srs.SrsReviewService;
import org.fpt.studydeck.service.srs.SrsStatsService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class SrsController {

    private final SrsReviewService srsReviewService;
    private final SrsStatsService srsStatsService;

    public SrsController(SrsReviewService srsReviewService, SrsStatsService srsStatsService) {
        this.srsReviewService = srsReviewService;
        this.srsStatsService = srsStatsService;
    }

    @GetMapping("/decks/{deckId}/srs/due")
    public List<SrsDueCardResponse> dueCards(@PathVariable("deckId") Long deckId) {
        return srsReviewService.dueCards(deckId, Instant.now());
    }

    @PostMapping("/decks/{deckId}/srs/review-sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public List<SrsDueCardResponse> createReviewSession(@PathVariable("deckId") Long deckId) {
        return srsReviewService.dueCards(deckId, Instant.now());
    }

    @GetMapping("/srs/review-sessions/{sessionId}")
    public void getReviewSession(@PathVariable("sessionId") Long sessionId) {
        throw new InvalidRequestException("SRS review sessions are not persisted in MVP.");
    }

    @PostMapping("/srs/review-sessions/{sessionId}/reviews")
    public void reviewSessionCard(@PathVariable("sessionId") Long sessionId) {
        throw new InvalidRequestException("SRS review sessions are not persisted in MVP.");
    }

    @PostMapping("/flashcards/{flashcardId}/srs/reviews")
    public SrsReviewResponse reviewFlashcard(
        @PathVariable("flashcardId") Long flashcardId,
        @Valid @RequestBody SrsReviewRequest request,
        Principal principal
    ) {
        return srsReviewService.review(flashcardId, request, principal.getName());
    }

    @GetMapping("/flashcards/{flashcardId}/srs-state")
    public SrsCardStateResponse getState(@PathVariable("flashcardId") Long flashcardId) {
        return srsReviewService.getState(flashcardId);
    }

    @GetMapping("/decks/{deckId}/srs/stats")
    public SrsStatsResponse stats(@PathVariable("deckId") Long deckId) {
        return srsStatsService.stats(deckId);
    }
}
