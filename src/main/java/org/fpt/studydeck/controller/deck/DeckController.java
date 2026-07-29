package org.fpt.studydeck.controller.deck;

import java.util.List;

import jakarta.validation.Valid;
import org.fpt.studydeck.dto.deck.CreateDeckRequest;
import org.fpt.studydeck.dto.deck.DeckResponse;
import org.fpt.studydeck.dto.deck.UpdateDeckRequest;
import org.fpt.studydeck.service.deck.DeckService;
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
public class DeckController {

    private final DeckService deckService;

    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    @GetMapping("/decks")
    public List<DeckResponse> listDecks() {
        return deckService.listDecks().stream()
                .map(DeckResponse::from)
                .toList();
    }

    @PostMapping("/decks")
    @ResponseStatus(HttpStatus.CREATED)
    public DeckResponse createDeck(@Valid @RequestBody CreateDeckRequest request) {
        return DeckResponse.from(deckService.createDeck(request.folderId(), request.title(), request.description()));
    }

    @GetMapping("/decks/{deckId}")
    public DeckResponse getDeck(@PathVariable("deckId") Long deckId) {
        return DeckResponse.from(deckService.getDeck(deckId));
    }

    @PatchMapping("/decks/{deckId}")
    public DeckResponse updateDeck(
            @PathVariable("deckId") Long deckId,
            @Valid @RequestBody UpdateDeckRequest request) {
        return DeckResponse.from(deckService.updateDeck(deckId, request.title(), request.description()));
    }

    @DeleteMapping("/decks/{deckId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDeck(@PathVariable("deckId") Long deckId) {
        deckService.deleteDeck(deckId);
    }

    @PostMapping("/folders/{folderId}/decks/{deckId}")
    public DeckResponse moveDeckToFolder(@PathVariable("folderId") Long folderId, @PathVariable("deckId") Long deckId) {
        return DeckResponse.from(deckService.moveDeckToFolder(folderId, deckId));
    }

    @DeleteMapping("/folders/{folderId}/decks/{deckId}")
    public DeckResponse removeDeckFromFolder(@PathVariable("folderId") Long folderId,
            @PathVariable("deckId") Long deckId) {
        return DeckResponse.from(deckService.removeDeckFromFolder(folderId, deckId));
    }
}
