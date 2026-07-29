package org.fpt.studydeck.service.learn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.fpt.studydeck.domain.deck.Deck;
import org.fpt.studydeck.domain.deck.Flashcard;
import org.fpt.studydeck.domain.learn.LearnQuestionType;
import org.fpt.studydeck.domain.learn.LearnSession;
import org.fpt.studydeck.domain.learn.LearnSessionItem;
import org.fpt.studydeck.domain.learn.LearnSessionStatus;
import org.fpt.studydeck.domain.learn.PromptSide;
import org.fpt.studydeck.dto.learn.CreateLearnSessionRequest;
import org.fpt.studydeck.dto.learn.LearnAnswerRequest;
import org.fpt.studydeck.dto.learn.LearnSessionItemResponse;
import org.fpt.studydeck.dto.learn.LearnSessionResponse;
import org.fpt.studydeck.exception.InvalidRequestException;
import org.fpt.studydeck.exception.ResourceConflictException;
import org.fpt.studydeck.exception.ResourceNotFoundException;
import org.fpt.studydeck.repository.deck.DeckRepository;
import org.fpt.studydeck.repository.deck.FlashcardRepository;
import org.fpt.studydeck.repository.learn.LearnSessionRepository;
import org.fpt.studydeck.service.gamification.DailyMissionService;
import org.fpt.studydeck.service.gamification.GamificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LearnSessionService {

    private static final String DECK_NOT_FOUND = "Deck was not found.";
    private static final String SESSION_NOT_FOUND = "Learn session was not found.";
    private static final String ITEM_NOT_FOUND = "Learn session item was not found.";
    private static final String NO_CARDS_AVAILABLE = "No cards are available for learn mode.";
    private static final String NO_QUESTION_TYPES = "At least one question type is required.";
    private static final String SESSION_COMPLETED = "Learn session is already completed.";
    private static final String NEGATIVE_LENGTH = "Length of rounds must be zero or positive.";

    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;
    private final LearnSessionRepository learnSessionRepository;
    private final GamificationService gamificationService;
    private final DailyMissionService dailyMissionService;

    public LearnSessionService(
        DeckRepository deckRepository,
        FlashcardRepository flashcardRepository,
        LearnSessionRepository learnSessionRepository,
        GamificationService gamificationService,
        DailyMissionService dailyMissionService
    ) {
        this.deckRepository = deckRepository;
        this.flashcardRepository = flashcardRepository;
        this.learnSessionRepository = learnSessionRepository;
        this.gamificationService = gamificationService;
        this.dailyMissionService = dailyMissionService;
    }

    public LearnSessionResponse createSession(Long deckId, CreateLearnSessionRequest request) {
        Deck deck = deckRepository.findById(deckId)
            .orElseThrow(() -> new ResourceNotFoundException(DECK_NOT_FOUND));
        CreateLearnSessionRequest effectiveRequest = request == null
            ? new CreateLearnSessionRequest(0, true, false, false, false, false, false)
            : request;
        if (effectiveRequest.lengthOfRounds() < 0) {
            throw new InvalidRequestException(NEGATIVE_LENGTH);
        }

        List<LearnQuestionType> questionTypes = questionTypes(effectiveRequest);
        if (questionTypes.isEmpty()) {
            throw new InvalidRequestException(NO_QUESTION_TYPES);
        }

        List<Flashcard> flashcards = effectiveRequest.starredOnly()
            ? flashcardRepository.findByDeckIdAndStarredTrueOrderByPositionAscIdAsc(deckId)
            : flashcardRepository.findByDeckIdOrderByPositionAscIdAsc(deckId);
        if (flashcards.isEmpty()) {
            throw new InvalidRequestException(NO_CARDS_AVAILABLE);
        }

        List<Flashcard> selectedCards = new ArrayList<>(flashcards);
        if (effectiveRequest.shuffleTerms()) {
            shuffle(selectedCards);
        }
        if (effectiveRequest.lengthOfRounds() > 0 && effectiveRequest.lengthOfRounds() < selectedCards.size()) {
            selectedCards = new ArrayList<>(selectedCards.subList(0, effectiveRequest.lengthOfRounds()));
        }

        LearnSession session = learnSessionRepository.save(
            LearnSession.create(deck, settingsJson(effectiveRequest), selectedCards, questionTypes)
        );
        return toResponse(session);
    }

    @Transactional(readOnly = true)
    public LearnSessionResponse getSession(Long sessionId) {
        return toResponse(findSession(sessionId));
    }

    public LearnSessionResponse answer(Long sessionId, LearnAnswerRequest request) {
        LearnSession session = findSession(sessionId);
        if (session.getStatus() == LearnSessionStatus.COMPLETED) {
            throw new ResourceConflictException(SESSION_COMPLETED);
        }

        LearnSessionItem item = session.getItems().stream()
            .filter(candidate -> candidate.getId().equals(request.itemId()))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException(ITEM_NOT_FOUND));

        item.answer(matchesExpectedAnswer(item, request.answer()));
        return toResponse(session);
    }

    public LearnSessionResponse complete(Long sessionId, String userEmail) {
        LearnSession session = findSession(sessionId);
        session.complete();
        gamificationService.recordActivity(userEmail);
        dailyMissionService.updateMissionProgress(userEmail, "learn_session_completed", 1, 1);
        gamificationService.awardPoints(userEmail, 10);
        return toResponse(session);
    }

    private LearnSession findSession(Long sessionId) {
        return learnSessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException(SESSION_NOT_FOUND));
    }

    private LearnSessionResponse toResponse(LearnSession session) {
        List<LearnSessionItem> orderedItems = orderedItems(session);
        return new LearnSessionResponse(
            session.getId(),
            session.getStatus().name(),
            orderedItems.size(),
            countCorrect(session),
            countWrong(session),
            orderedItems.stream().map(LearnSessionItemResponse::from).toList()
        );
    }

    private int countCorrect(LearnSession session) {
        return session.getItems().stream()
            .mapToInt(LearnSessionItem::getCorrectCount)
            .sum();
    }

    private int countWrong(LearnSession session) {
        return session.getItems().stream()
            .mapToInt(LearnSessionItem::getWrongCount)
            .sum();
    }

    private List<LearnSessionItem> orderedItems(LearnSession session) {
        return session.getItems().stream()
            .sorted(Comparator.comparingInt(LearnSessionItem::getPosition))
            .toList();
    }

    private boolean matchesExpectedAnswer(LearnSessionItem item, String submitted) {
        if (item.getQuestionType() == LearnQuestionType.TRUE_FALSE) {
            return submitted != null && "true".equalsIgnoreCase(submitted.trim());
        }

        String expected = item.getPromptSide() == PromptSide.TERM
            ? item.getFlashcard().getDefinition()
            : item.getFlashcard().getTerm();
        return submitted != null && submitted.trim().equalsIgnoreCase(expected.trim());
    }

    private List<LearnQuestionType> questionTypes(CreateLearnSessionRequest request) {
        List<LearnQuestionType> questionTypes = new ArrayList<>();
        if (request.flashcards()) {
            questionTypes.add(LearnQuestionType.FLASHCARD);
        }
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

    private String settingsJson(CreateLearnSessionRequest request) {
        return """
            {"lengthOfRounds":%d,"flashcards":%s,"multipleChoice":%s,"written":%s,"trueFalse":%s,"starredOnly":%s,"shuffleTerms":%s}"""
            .formatted(
                request.lengthOfRounds(),
                request.flashcards(),
                request.multipleChoice(),
                request.written(),
                request.trueFalse(),
                request.starredOnly(),
                request.shuffleTerms()
            );
    }

    private void shuffle(List<Flashcard> cards) {
        if (cards.size() <= 1) {
            return;
        }

        List<Flashcard> original = List.copyOf(cards);
        Collections.shuffle(cards);
        if (cards.equals(original)) {
            Collections.swap(cards, 0, 1);
        }
    }
}
