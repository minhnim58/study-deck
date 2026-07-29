package org.fpt.studydeck.controller.deck;

import java.util.List;

import org.fpt.studydeck.dto.deck.ViewerCardResponse;
import org.fpt.studydeck.service.deck.ViewerCardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ViewerCardController {

    private final ViewerCardService viewerCardService;

    public ViewerCardController(ViewerCardService viewerCardService) {
        this.viewerCardService = viewerCardService;
    }

    @GetMapping("/decks/{deckId}/viewer-cards")
    public List<ViewerCardResponse> getCards(
        @PathVariable("deckId") Long deckId,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String mode
    ) {
        return viewerCardService.getCards(deckId, sort, mode);
    }
}
