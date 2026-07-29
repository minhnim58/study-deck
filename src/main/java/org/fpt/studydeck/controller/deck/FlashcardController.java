package org.fpt.studydeck.controller.deck;

import java.util.List;

import jakarta.validation.Valid;
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
    public List<FlashcardResponse> listDeckFlashcards(@PathVariable("deckId") Long deckId) {
        return flashcardService.listDeckFlashcards(deckId).stream()
            .map(FlashcardResponse::from)
            .toList();
    }

    @PostMapping("/decks/{deckId}/flashcards")
    @ResponseStatus(HttpStatus.CREATED)
    public FlashcardResponse createFlashcard(
        @PathVariable("deckId") Long deckId,
        @Valid @RequestBody CreateFlashcardRequest request
    ) {
        return FlashcardResponse.from(flashcardService.createFlashcard(
            deckId,
            request.term(),
            request.definition(),
            request.termImageUrl(),
            request.definitionImageUrl()
        ));
    }

    @PatchMapping("/flashcards/{flashcardId}")
    public FlashcardResponse updateFlashcard(
        @PathVariable("flashcardId") Long flashcardId,
        @Valid @RequestBody UpdateFlashcardRequest request
    ) {
        return FlashcardResponse.from(flashcardService.updateFlashcard(
            flashcardId,
            request.term(),
            request.definition(),
            request.termImageUrl(),
            request.definitionImageUrl()
        ));
    }

    @DeleteMapping("/flashcards/{flashcardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFlashcard(@PathVariable("flashcardId") Long flashcardId) {
        flashcardService.deleteFlashcard(flashcardId);
    }

    @PatchMapping("/flashcards/{flashcardId}/star")
    public FlashcardResponse setStarred(
        @PathVariable("flashcardId") Long flashcardId,
        @Valid @RequestBody StarFlashcardRequest request
    ) {
        return FlashcardResponse.from(flashcardService.setStarred(flashcardId, request.starred()));
    }
}
