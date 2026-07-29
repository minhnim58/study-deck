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
import java.security.Principal;
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
    public List<DeckResponse> listDecks(Principal principal) {
        return deckService.listDecks(principal.getName()).stream()
                .map(DeckResponse::from)
                .toList();
    }

    @PostMapping("/decks")
    @ResponseStatus(HttpStatus.CREATED)
    public DeckResponse createDeck(Principal principal, @Valid @RequestBody CreateDeckRequest request) {
        return DeckResponse.from(deckService.createDeck(principal.getName(), request.folderId(), request.title(),
                request.description(), request.visibility()));
    }

    @GetMapping("/decks/{deckId}")
    public DeckResponse getDeck(Principal principal, @PathVariable("deckId") Long deckId) {
        return DeckResponse.from(deckService.getDeck(deckId, principal.getName()));
    }

    @PatchMapping("/decks/{deckId}")
    public DeckResponse updateDeck(
            Principal principal,
            @PathVariable("deckId") Long deckId,
            @Valid @RequestBody UpdateDeckRequest request) {
        return DeckResponse.from(deckService.updateDeck(principal.getName(), deckId, request.title(),
                request.description(), request.visibility()));
    }

    @DeleteMapping("/decks/{deckId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDeck(Principal principal, @PathVariable("deckId") Long deckId) {
        deckService.deleteDeck(principal.getName(), deckId);
    }

    @PostMapping("/folders/{folderId}/decks/{deckId}")
    public DeckResponse moveDeckToFolder(Principal principal, @PathVariable("folderId") Long folderId,
            @PathVariable("deckId") Long deckId) {
        return DeckResponse.from(deckService.moveDeckToFolder(principal.getName(), folderId, deckId));
    }

    @DeleteMapping("/folders/{folderId}/decks/{deckId}")
    public DeckResponse removeDeckFromFolder(Principal principal, @PathVariable("folderId") Long folderId,
            @PathVariable("deckId") Long deckId) {
        return DeckResponse.from(deckService.removeDeckFromFolder(principal.getName(), folderId, deckId));
    }
}
