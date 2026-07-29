package org.fpt.studydeck.controller.sorting;

import jakarta.validation.Valid;
import org.fpt.studydeck.dto.sorting.CreateSortingSessionRequest;
import org.fpt.studydeck.dto.sorting.SortingAnswerRequest;
import org.fpt.studydeck.dto.sorting.SortingSessionResponse;
import org.fpt.studydeck.service.sorting.SortingSessionService;
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
public class SortingSessionController {

    private final SortingSessionService sortingSessionService;

    public SortingSessionController(SortingSessionService sortingSessionService) {
        this.sortingSessionService = sortingSessionService;
    }

    @PostMapping("/decks/{deckId}/sorting-sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public SortingSessionResponse createSession(
        @PathVariable("deckId") Long deckId,
        @RequestBody(required = false) CreateSortingSessionRequest request
    ) {
        return sortingSessionService.createSession(deckId, request);
    }

    @GetMapping("/sorting-sessions/{sessionId}")
    public SortingSessionResponse getSession(@PathVariable("sessionId") Long sessionId) {
        return sortingSessionService.getSession(sessionId);
    }

    @PostMapping("/sorting-sessions/{sessionId}/answers")
    public SortingSessionResponse answer(
        @PathVariable("sessionId") Long sessionId,
        @Valid @RequestBody SortingAnswerRequest request
    ) {
        return sortingSessionService.answer(sessionId, request);
    }
}
