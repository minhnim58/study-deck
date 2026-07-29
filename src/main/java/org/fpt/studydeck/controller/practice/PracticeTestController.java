package org.fpt.studydeck.controller.practice;

import java.security.Principal;

import org.fpt.studydeck.dto.practice.CreatePracticeTestRequest;
import org.fpt.studydeck.dto.practice.PracticeAnswerRequest;
import org.fpt.studydeck.dto.practice.PracticeTestResponse;
import org.fpt.studydeck.service.practice.PracticeTestService;
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
public class PracticeTestController {

    private final PracticeTestService practiceTestService;

    public PracticeTestController(PracticeTestService practiceTestService) {
        this.practiceTestService = practiceTestService;
    }

    @PostMapping("/decks/{deckId}/practice-tests")
    @ResponseStatus(HttpStatus.CREATED)
    public PracticeTestResponse createPracticeTest(
        @PathVariable("deckId") Long deckId,
        @Valid @RequestBody CreatePracticeTestRequest request
    ) {
        return practiceTestService.createPracticeTest(deckId, request);
    }

    @GetMapping("/practice-tests/{practiceTestId}")
    public PracticeTestResponse getPracticeTest(@PathVariable("practiceTestId") Long practiceTestId) {
        return practiceTestService.getPracticeTest(practiceTestId);
    }

    @PostMapping("/practice-tests/{practiceTestId}/answers")
    public PracticeTestResponse answer(
        @PathVariable("practiceTestId") Long practiceTestId,
        @Valid @RequestBody PracticeAnswerRequest request
    ) {
        return practiceTestService.answer(practiceTestId, request);
    }

    @PostMapping("/practice-tests/{practiceTestId}/submit")
    public PracticeTestResponse submit(@PathVariable("practiceTestId") Long practiceTestId, Principal principal) {
        return practiceTestService.submit(practiceTestId, principal.getName());
    }
}
