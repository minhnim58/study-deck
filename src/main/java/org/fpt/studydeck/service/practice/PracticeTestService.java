package org.fpt.studydeck.service.practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.fpt.studydeck.domain.deck.Deck;
import org.fpt.studydeck.domain.deck.Flashcard;
import org.fpt.studydeck.domain.learn.LearnQuestionType;
import org.fpt.studydeck.domain.learn.PromptSide;
import org.fpt.studydeck.domain.practice.PracticeTest;
import org.fpt.studydeck.domain.practice.PracticeTestQuestion;
import org.fpt.studydeck.domain.practice.PracticeTestStatus;
import org.fpt.studydeck.dto.practice.CreatePracticeTestRequest;
import org.fpt.studydeck.dto.practice.PracticeAnswerRequest;
import org.fpt.studydeck.dto.practice.PracticeQuestionResponse;
import org.fpt.studydeck.dto.practice.PracticeTestResponse;
import org.fpt.studydeck.exception.InvalidRequestException;
import org.fpt.studydeck.exception.ResourceConflictException;
import org.fpt.studydeck.exception.ResourceNotFoundException;
import org.fpt.studydeck.repository.deck.DeckRepository;
import org.fpt.studydeck.repository.deck.FlashcardRepository;
import org.fpt.studydeck.repository.practice.PracticeTestRepository;
import org.fpt.studydeck.service.gamification.DailyMissionService;
import org.fpt.studydeck.service.gamification.GamificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PracticeTestService {

    private static final String DECK_NOT_FOUND = "Deck was not found.";
    private static final String TEST_NOT_FOUND = "Practice test was not found.";
    private static final String QUESTION_NOT_FOUND = "Practice test question was not found.";
    private static final String NO_QUESTION_TYPES = "At least one question type is required.";
    private static final String NO_ANSWER_FORMAT = "At least one answer format is required.";
    private static final String NOT_ENOUGH_CARDS = "Not enough cards are available for practice test.";
    private static final String TEST_SUBMITTED = "Practice test is already submitted.";
    private static final String NON_POSITIVE_QUESTION_COUNT = "Question count must be positive.";

    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;
    private final PracticeTestRepository practiceTestRepository;
    private final GamificationService gamificationService;
    private final DailyMissionService dailyMissionService;

    public PracticeTestService(
            DeckRepository deckRepository,
            FlashcardRepository flashcardRepository,
            PracticeTestRepository practiceTestRepository,
            GamificationService gamificationService,
            DailyMissionService dailyMissionService) {
        this.deckRepository = deckRepository;
        this.flashcardRepository = flashcardRepository;
        this.practiceTestRepository = practiceTestRepository;
        this.gamificationService = gamificationService;
        this.dailyMissionService = dailyMissionService;
    }

    public PracticeTestResponse createPracticeTest(Long deckId, CreatePracticeTestRequest request) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new ResourceNotFoundException(DECK_NOT_FOUND));
        if (request == null || request.questionCount() <= 0) {
            throw new InvalidRequestException(NON_POSITIVE_QUESTION_COUNT);
        }

        List<LearnQuestionType> questionTypes = questionTypes(request);
        if (questionTypes.isEmpty()) {
            throw new InvalidRequestException(NO_QUESTION_TYPES);
        }
        if (!request.answerWithTerm() && !request.answerWithDefinition()) {
            throw new InvalidRequestException(NO_ANSWER_FORMAT);
        }

        List<Flashcard> availableCards = request.starredOnly()
                ? flashcardRepository.findByDeckIdAndStarredTrueOrderByPositionAscIdAsc(deckId)
                : flashcardRepository.findByDeckIdOrderByPositionAscIdAsc(deckId);
        if (availableCards.size() < request.questionCount()) {
            throw new InvalidRequestException(NOT_ENOUGH_CARDS);
        }

        List<Flashcard> selectedCards = new ArrayList<>(availableCards.subList(0, request.questionCount()));
        PracticeTest practiceTest = practiceTestRepository.save(
                PracticeTest.create(deck, settingsJson(request), selectedCards, questionTypes, promptSides(request)));
        return toResponse(practiceTest, availableCards);
    }

    @Transactional(readOnly = true)
    public PracticeTestResponse getPracticeTest(Long testId) {
        PracticeTest practiceTest = findPracticeTest(testId);
        List<Flashcard> allCards = flashcardRepository.findByDeckIdOrderByPositionAscIdAsc(
                practiceTest.getDeck().getId());
        return toResponse(practiceTest, allCards);
    }

    public PracticeTestResponse answer(Long testId, PracticeAnswerRequest request) {
        PracticeTest practiceTest = findPracticeTest(testId);
        if (practiceTest.getStatus() == PracticeTestStatus.SUBMITTED) {
            throw new ResourceConflictException(TEST_SUBMITTED);
        }

        PracticeTestQuestion question = practiceTest.getQuestions().stream()
                .filter(candidate -> candidate.getId().equals(request.questionId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(QUESTION_NOT_FOUND));
        question.answer(request.answer(), matchesExpectedAnswer(question, request.answer()));
        List<Flashcard> allCards = flashcardRepository.findByDeckIdOrderByPositionAscIdAsc(
                practiceTest.getDeck().getId());
        return toResponse(practiceTest, allCards);
    }

    public PracticeTestResponse submit(Long testId) {
        return submit(testId, null);
    }

    public PracticeTestResponse submit(Long testId, String userEmail) {
        PracticeTest practiceTest = findPracticeTest(testId);
        practiceTest.submit();
        if (userEmail != null && !userEmail.isBlank()) {
            gamificationService.recordActivity(userEmail);
            dailyMissionService.updateMissionProgress(userEmail, "practice_test_completed", 1, 1);
            gamificationService.awardPoints(userEmail, 20);
        }
        List<Flashcard> allCards = flashcardRepository.findByDeckIdOrderByPositionAscIdAsc(
                practiceTest.getDeck().getId());
        return toResponse(practiceTest, allCards);
    }

    private PracticeTest findPracticeTest(Long testId) {
        return practiceTestRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException(TEST_NOT_FOUND));
    }

    private PracticeTestResponse toResponse(PracticeTest practiceTest, List<Flashcard> allCards) {
        List<PracticeTestQuestion> orderedQuestions = orderedQuestions(practiceTest);
        return new PracticeTestResponse(
                practiceTest.getId(),
                practiceTest.getStatus().name(),
                orderedQuestions.size(),
                answeredCount(practiceTest),
                practiceTest.getScorePercent(),
                orderedQuestions.stream()
                        .map(question -> PracticeQuestionResponse.from(question, generateOptions(question, allCards)))
                        .toList());
    }

    private List<String> generateOptions(PracticeTestQuestion question, List<Flashcard> allCards) {
        if (question.getQuestionType() != LearnQuestionType.MULTIPLE_CHOICE) {
            return List.of();
        }

        String correctAnswer = question.getCorrectAnswer();
        boolean useDefinitions = question.getPromptSide() == PromptSide.TERM;

        List<String> distractors = new ArrayList<>();
        for (Flashcard card : allCards) {
            String candidateAnswer = useDefinitions ? card.getDefinition() : card.getTerm();
            if (!candidateAnswer.equalsIgnoreCase(correctAnswer) && !distractors.contains(candidateAnswer)) {
                distractors.add(candidateAnswer);
            }
        }
        Collections.shuffle(distractors);
        int distractorCount = Math.min(3, distractors.size());
        List<String> options = new ArrayList<>(distractors.subList(0, distractorCount));
        options.add(correctAnswer);
        Collections.shuffle(options);
        return options;
    }

    private List<PracticeTestQuestion> orderedQuestions(PracticeTest practiceTest) {
        return practiceTest.getQuestions().stream()
                .sorted(Comparator.comparingInt(PracticeTestQuestion::getPosition))
                .toList();
    }

    private int answeredCount(PracticeTest practiceTest) {
        return (int) practiceTest.getQuestions().stream()
                .filter(question -> question.getSubmittedAnswer() != null)
                .count();
    }

    private boolean matchesExpectedAnswer(PracticeTestQuestion question, String submittedAnswer) {
        return submittedAnswer != null
                && submittedAnswer.trim().equalsIgnoreCase(question.getCorrectAnswer().trim());
    }

    private List<LearnQuestionType> questionTypes(CreatePracticeTestRequest request) {
        List<LearnQuestionType> questionTypes = new ArrayList<>();
        if (request.multipleChoice()) {
            questionTypes.add(LearnQuestionType.MULTIPLE_CHOICE);
        }
        if (request.written()) {
            questionTypes.add(LearnQuestionType.WRITTEN);
        }
        if (request.trueFalse()) {
            questionTypes.add(LearnQuestionType.TRUE_FALSE);
        }
        return questionTypes;
    }

    private List<PromptSide> promptSides(CreatePracticeTestRequest request) {
        List<PromptSide> promptSides = new ArrayList<>();
        if (request.answerWithDefinition()) {
            promptSides.add(PromptSide.TERM);
        }
        if (request.answerWithTerm()) {
            promptSides.add(PromptSide.DEFINITION);
        }
        return promptSides;
    }

    private String settingsJson(CreatePracticeTestRequest request) {
        return """
                {"questionCount":%d,"multipleChoice":%s,"written":%s,"trueFalse":%s,"starredOnly":%s,"answerWithTerm":%s,"answerWithDefinition":%s}"""
                .formatted(
                        request.questionCount(),
                        request.multipleChoice(),
                        request.written(),
                        request.trueFalse(),
                        request.starredOnly(),
                        request.answerWithTerm(),
                        request.answerWithDefinition());
    }
}
