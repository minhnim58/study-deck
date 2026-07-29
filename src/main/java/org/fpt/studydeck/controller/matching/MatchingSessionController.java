package org.fpt.studydeck.controller.matching;

import org.fpt.studydeck.dto.matching.CreateMatchingSessionRequest;
import org.fpt.studydeck.dto.matching.MatchingAnswerRequest;
import org.fpt.studydeck.dto.matching.MatchingSessionResponse;
import org.fpt.studydeck.service.matching.MatchingSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class MatchingSessionController {

    private final MatchingSessionService matchingSessionService;

    public MatchingSessionController(MatchingSessionService matchingSessionService) {
        this.matchingSessionService = matchingSessionService;
    }

    @PostMapping("/decks/{deckId}/matching-sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public MatchingSessionResponse createSession(
            @PathVariable("deckId") Long deckId,
            @Valid @RequestBody CreateMatchingSessionRequest request) {
        return matchingSessionService.createSession(deckId, request);
    }

    @GetMapping("/matching-sessions/{sessionId}")
    public MatchingSessionResponse getSession(@PathVariable("sessionId") Long sessionId) {
        return matchingSessionService.getSession(sessionId);
    }

    @PostMapping("/matching-sessions/{sessionId}/matches")
    public MatchingSessionResponse match(
            @PathVariable("sessionId") Long sessionId,
            @Valid @RequestBody MatchingAnswerRequest request,
            Principal principal) {
        String email = principal != null ? principal.getName() : null;
        return matchingSessionService.match(sessionId, request, email);
    }

    @PostMapping("/matching-sessions/{sessionId}/complete")
    public MatchingSessionResponse complete(@PathVariable("sessionId") Long sessionId, Principal principal) {
        String email = principal != null ? principal.getName() : null;
        return matchingSessionService.complete(sessionId, email);
    }
}
