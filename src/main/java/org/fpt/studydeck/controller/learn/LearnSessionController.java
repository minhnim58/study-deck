package org.fpt.studydeck.controller.learn;

import java.security.Principal;

import org.fpt.studydeck.dto.learn.CreateLearnSessionRequest;
import org.fpt.studydeck.dto.learn.LearnAnswerRequest;
import org.fpt.studydeck.dto.learn.LearnSessionResponse;
import org.fpt.studydeck.service.learn.LearnSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class LearnSessionController {

    private final LearnSessionService learnSessionService;

    public LearnSessionController(LearnSessionService learnSessionService) {
        this.learnSessionService = learnSessionService;
    }

    @PostMapping("/decks/{deckId}/learn-sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public LearnSessionResponse createSession(
        @PathVariable("deckId") Long deckId,
        @Valid @RequestBody(required = false) CreateLearnSessionRequest request
    ) {
        return learnSessionService.createSession(deckId, request);
    }

    @GetMapping("/learn-sessions/{sessionId}")
    public LearnSessionResponse getSession(@PathVariable("sessionId") Long sessionId) {
        return learnSessionService.getSession(sessionId);
    }

    @PostMapping("/learn-sessions/{sessionId}/answers")
    public LearnSessionResponse answer(
        @PathVariable("sessionId") Long sessionId,
        @Valid @RequestBody LearnAnswerRequest request
    ) {
        return learnSessionService.answer(sessionId, request);
    }

    @PostMapping("/learn-sessions/{sessionId}/complete")
    public LearnSessionResponse complete(@PathVariable("sessionId") Long sessionId, Principal principal) {
        return learnSessionService.complete(sessionId, principal.getName());
    }
}
