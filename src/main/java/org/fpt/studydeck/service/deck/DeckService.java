package org.fpt.studydeck.service.deck;

import java.util.List;

import java.util.stream.Collectors;

import org.fpt.studydeck.domain.auth.AppUser;
import org.fpt.studydeck.domain.deck.Deck;
import org.fpt.studydeck.domain.deck.Folder;
import org.fpt.studydeck.domain.deck.Visibility;
import org.fpt.studydeck.exception.ResourceNotFoundException;
import org.fpt.studydeck.exception.AccessDeniedException;
import org.fpt.studydeck.repository.auth.AppUserRepository;
import org.fpt.studydeck.repository.deck.DeckRepository;
import org.fpt.studydeck.repository.deck.FlashcardRepository;
import org.fpt.studydeck.repository.deck.FolderRepository;
import org.fpt.studydeck.repository.learn.LearnSessionRepository;
import org.fpt.studydeck.repository.matching.MatchingSessionRepository;
import org.fpt.studydeck.repository.practice.PracticeTestRepository;
import org.fpt.studydeck.repository.sorting.SortingSessionRepository;
import org.fpt.studydeck.repository.srs.SrsCardStateRepository;
import org.fpt.studydeck.repository.srs.SrsReviewLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

@Service
@Transactional
public class DeckService {

    private static final String DECK_NOT_FOUND = "Deck was not found.";
    private static final String FOLDER_NOT_FOUND = "Folder was not found.";

    private final DeckRepository deckRepository;
    private final FolderRepository folderRepository;
    private final FlashcardRepository flashcardRepository;
    private final LearnSessionRepository learnSessionRepository;
    private final MatchingSessionRepository matchingSessionRepository;
    private final PracticeTestRepository practiceTestRepository;
    private final SortingSessionRepository sortingSessionRepository;
    private final SrsCardStateRepository srsCardStateRepository;
    private final SrsReviewLogRepository srsReviewLogRepository;
    private final AppUserRepository userRepository;
    private final EntityManager entityManager;

    public DeckService(
            DeckRepository deckRepository,
            FolderRepository folderRepository,
            FlashcardRepository flashcardRepository,
            LearnSessionRepository learnSessionRepository,
            MatchingSessionRepository matchingSessionRepository,
            PracticeTestRepository practiceTestRepository,
            SortingSessionRepository sortingSessionRepository,
            SrsCardStateRepository srsCardStateRepository,
            SrsReviewLogRepository srsReviewLogRepository,
            AppUserRepository userRepository,
            EntityManager entityManager) {
        this.deckRepository = deckRepository;
        this.folderRepository = folderRepository;
        this.flashcardRepository = flashcardRepository;
        this.learnSessionRepository = learnSessionRepository;
        this.matchingSessionRepository = matchingSessionRepository;
        this.practiceTestRepository = practiceTestRepository;
        this.sortingSessionRepository = sortingSessionRepository;
        this.srsCardStateRepository = srsCardStateRepository;
        this.srsReviewLogRepository = srsReviewLogRepository;
        this.userRepository = userRepository;
        this.entityManager = entityManager;
    }

    public Deck createDeck(String userEmail, Long folderId, String title, String description, Visibility visibility) {
        AppUser creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Folder folder = null;
        if (folderId != null) {
            folder = folderRepository.findById(folderId)
                    .orElseThrow(() -> new ResourceNotFoundException(FOLDER_NOT_FOUND));
        }
        return deckRepository.save(Deck.create(folder, title, description, visibility, creator));
    }

    // Legacy method for existing unit tests
    public Deck createDeck(Long folderId, String title, String description) {
        Folder folder = null;
        if (folderId != null) {
            folder = folderRepository.findById(folderId).orElse(null);
        }
        return deckRepository.save(Deck.create(folder, title, description, Visibility.PRIVATE, null));
    }

    @Transactional(readOnly = true)
    public Deck getDeck(Long id, String userEmail) {
        Deck deck = deckRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(DECK_NOT_FOUND));
        if (deck.getVisibility() == Visibility.PRIVATE) {
            verifyOwnership(deck, userEmail);
        }
        return deck;
    }

    // Legacy method for existing unit tests
    @Transactional(readOnly = true)
    public Deck getDeck(Long id) {
        return deckRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(DECK_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<Deck> listDecks(String userEmail) {
        AppUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return deckRepository.findAll().stream()
                .filter(d -> d.getVisibility() == Visibility.PUBLIC ||
                        (d.getCreator() != null && d.getCreator().getId().equals(user.getId())))
                .collect(Collectors.toList());
    }

    public Deck updateDeck(String userEmail, Long id, String title, String description, Visibility visibility) {
        Deck deck = deckRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(DECK_NOT_FOUND));
        verifyOwnership(deck, userEmail);
        deck.update(title, description, visibility);
        return deck;
    }

    // Legacy method for existing unit tests
    public Deck updateDeck(Long id, String title, String description) {
        Deck deck = deckRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(DECK_NOT_FOUND));
        deck.update(title, description, Visibility.PRIVATE);
        return deck;
    }

    public Deck moveDeckToFolder(String userEmail, Long folderId, Long deckId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException(FOLDER_NOT_FOUND));
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new ResourceNotFoundException(DECK_NOT_FOUND));

        verifyOwnership(deck, userEmail);
        verifyFolderOwnership(folder, userEmail);

        deck.moveToFolder(folder);
        return deck;
    }

    // Legacy method for existing unit tests
    public Deck moveDeckToFolder(Long folderId, Long deckId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException(FOLDER_NOT_FOUND));
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new ResourceNotFoundException(DECK_NOT_FOUND));
        deck.moveToFolder(folder);
        return deck;
    }

    public Deck removeDeckFromFolder(String userEmail, Long folderId, Long deckId) {
        if (!folderRepository.existsById(folderId)) {
            throw new ResourceNotFoundException(FOLDER_NOT_FOUND);
        }
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new ResourceNotFoundException(DECK_NOT_FOUND));

        verifyOwnership(deck, userEmail);

        if (deck.getFolder() != null && deck.getFolder().getId().equals(folderId)) {
            deck.moveToFolder(null);
        }
        return deck;
    }

    // Legacy method for existing unit tests
    public Deck removeDeckFromFolder(Long folderId, Long deckId) {
        if (!folderRepository.existsById(folderId)) {
            throw new ResourceNotFoundException(FOLDER_NOT_FOUND);
        }
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new ResourceNotFoundException(DECK_NOT_FOUND));

        if (deck.getFolder() != null && deck.getFolder().getId().equals(folderId)) {
            deck.moveToFolder(null);
        }
        return deck;
    }

    public void deleteDeck(String userEmail, Long id) {
        Deck deck = deckRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(DECK_NOT_FOUND));
        verifyOwnership(deck, userEmail);

        learnSessionRepository.deleteItemsByFlashcardDeckId(id);
        matchingSessionRepository.deleteItemsByFlashcardDeckId(id);
        practiceTestRepository.deleteQuestionsByFlashcardDeckId(id);
        sortingSessionRepository.deleteItemsByFlashcardDeckId(id);
        learnSessionRepository.deleteByDeckId(id);
        matchingSessionRepository.deleteByDeckId(id);
        practiceTestRepository.deleteByDeckId(id);
        sortingSessionRepository.deleteByDeckId(id);
        srsReviewLogRepository.deleteByFlashcardDeckId(id);
        srsCardStateRepository.deleteByFlashcardDeckId(id);
        flashcardRepository.deleteByDeckId(id);
        entityManager.flush();
        entityManager.clear();
        deckRepository.deleteById(id);
    }

    // Legacy method for existing unit tests
    public void deleteDeck(Long id) {
        Deck deck = deckRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(DECK_NOT_FOUND));

        learnSessionRepository.deleteItemsByFlashcardDeckId(id);
        matchingSessionRepository.deleteItemsByFlashcardDeckId(id);
        practiceTestRepository.deleteQuestionsByFlashcardDeckId(id);
        sortingSessionRepository.deleteItemsByFlashcardDeckId(id);
        learnSessionRepository.deleteByDeckId(id);
        matchingSessionRepository.deleteByDeckId(id);
        practiceTestRepository.deleteByDeckId(id);
        sortingSessionRepository.deleteByDeckId(id);
        srsReviewLogRepository.deleteByFlashcardDeckId(id);
        srsCardStateRepository.deleteByFlashcardDeckId(id);
        flashcardRepository.deleteByDeckId(id);
        entityManager.flush();
        entityManager.clear();
        deckRepository.deleteById(id);
    }

    private void verifyOwnership(Deck deck, String userEmail) {
        if (deck.getCreator() == null || !deck.getCreator().getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("You do not have permission to access or modify this deck.");
        }
    }

    private void verifyFolderOwnership(Folder folder, String userEmail) {
        if (folder.getCreator() == null || !folder.getCreator().getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("You do not have permission to modify this folder.");
        }
    }
}
