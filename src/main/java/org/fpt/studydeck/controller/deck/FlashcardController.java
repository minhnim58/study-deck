package org.fpt.studydeck.controller.deck;

import java.security.Principal;
import java.util.List;

import jakarta.validation.Valid;
import org.fpt.studydeck.domain.deck.Flashcard;
import org.fpt.studydeck.dto.deck.CreateFlashcardRequest;
import org.fpt.studydeck.dto.deck.FlashcardResponse;
import org.fpt.studydeck.dto.deck.StarFlashcardRequest;
import org.fpt.studydeck.dto.deck.UpdateFlashcardRequest;
import org.fpt.studydeck.service.deck.FlashcardService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class FlashcardController {

    private final FlashcardService flashcardService;

    public FlashcardController(FlashcardService flashcardService) {
        this.flashcardService = flashcardService;
    }

    @GetMapping("/decks/{deckId}/flashcards")
    public List<FlashcardResponse> listDeckFlashcards(java.security.Principal principal,
            @PathVariable("deckId") Long deckId) {
        String email = principal != null ? principal.getName() : null;
        return flashcardService.listDeckFlashcards(deckId).stream()
                .map(f -> FlashcardResponse.from(f, email))
                .toList();
    }

    @PostMapping("/decks/{deckId}/flashcards")
    @ResponseStatus(HttpStatus.CREATED)
    public FlashcardResponse createFlashcard(
            @PathVariable Long deckId,
            @Valid @RequestBody CreateFlashcardRequest request,
            Principal principal) {
        Flashcard flashcard = flashcardService.createFlashcard(
                deckId,
                request.term(),
                request.definition(),
                request.termImageUrl(),
                request.definitionImageUrl());
        return FlashcardResponse.from(flashcard, principal != null ? principal.getName() : null);
    }

    @PostMapping("/decks/{deckId}/flashcards/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<FlashcardResponse> createFlashcards(
            @PathVariable Long deckId,
            @Valid @RequestBody List<CreateFlashcardRequest> requests,
            Principal principal) {
        List<Flashcard> flashcards = flashcardService.createFlashcards(deckId, requests);
        String userEmail = principal != null ? principal.getName() : null;
        return flashcards.stream()
                .map(flashcard -> FlashcardResponse.from(flashcard, userEmail))
                .toList();
    }

    @PatchMapping("/flashcards/{flashcardId}")
    public FlashcardResponse updateFlashcard(
            java.security.Principal principal,
            @PathVariable("flashcardId") Long flashcardId,
            @Valid @RequestBody UpdateFlashcardRequest request) {
        String email = principal != null ? principal.getName() : null;
        return FlashcardResponse.from(flashcardService.updateFlashcard(
                flashcardId,
                request.term(),
                request.definition(),
                request.termImageUrl(),
                request.definitionImageUrl()), email);
    }

    @DeleteMapping("/flashcards/{flashcardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFlashcard(@PathVariable("flashcardId") Long flashcardId) {
        flashcardService.deleteFlashcard(flashcardId);
    }

    @PatchMapping("/flashcards/{flashcardId}/star")
    public FlashcardResponse setStarred(
            java.security.Principal principal,
            @PathVariable("flashcardId") Long flashcardId,
            @Valid @RequestBody StarFlashcardRequest request) {
        return FlashcardResponse.from(
                flashcardService.setStarred(flashcardId, principal.getName(), request.starred()),
                principal.getName());
    }
}
