package org.fpt.studydeck.controller.deck;

import org.fpt.studydeck.dto.deck.DeckSummaryResponse;
import org.fpt.studydeck.service.deck.DeckSummaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class DeckSummaryController {

    private final DeckSummaryService deckSummaryService;

    public DeckSummaryController(DeckSummaryService deckSummaryService) {
        this.deckSummaryService = deckSummaryService;
    }

    @GetMapping("/decks/{deckId}/summary")
    public DeckSummaryResponse getSummary(@PathVariable("deckId") Long deckId) {
        return deckSummaryService.getSummary(deckId);
    }
}
