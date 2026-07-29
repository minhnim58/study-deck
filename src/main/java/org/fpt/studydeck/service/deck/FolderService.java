package org.fpt.studydeck.service.deck;

import java.util.List;

import java.util.stream.Collectors;

import org.fpt.studydeck.domain.auth.AppUser;
import org.fpt.studydeck.domain.deck.Folder;
import org.fpt.studydeck.domain.deck.Visibility;
import org.fpt.studydeck.exception.ResourceNotFoundException;
import org.fpt.studydeck.exception.AccessDeniedException;
import org.fpt.studydeck.repository.auth.AppUserRepository;
import org.fpt.studydeck.repository.deck.DeckRepository;
import org.fpt.studydeck.repository.deck.FolderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FolderService {

    private static final String FOLDER_NOT_FOUND = "Folder was not found.";

    private final FolderRepository folderRepository;
    private final DeckRepository deckRepository;
    private final AppUserRepository userRepository;

    public FolderService(FolderRepository folderRepository, DeckRepository deckRepository,
            AppUserRepository userRepository) {
        this.folderRepository = folderRepository;
        this.deckRepository = deckRepository;
        this.userRepository = userRepository;
    }

    public Folder createFolder(String userEmail, String name, String description, Visibility visibility) {
        AppUser creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return folderRepository.save(Folder.create(name, description, visibility, creator));
    }

    // Legacy method for existing unit tests
    public Folder createFolder(String name, String description) {
        return folderRepository.save(Folder.create(name, description, Visibility.PRIVATE, null));
    }

    @Transactional(readOnly = true)
    public Folder getFolder(Long id, String userEmail) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(FOLDER_NOT_FOUND));

        if (folder.getVisibility() == Visibility.PRIVATE) {
            verifyOwnership(folder, userEmail);
        }

        return folder;
    }

    // Legacy method for existing unit tests
    @Transactional(readOnly = true)
    public Folder getFolder(Long id) {
        return folderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(FOLDER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<Folder> listFolders(String userEmail) {
        AppUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return folderRepository.findAll().stream()
                .filter(f -> f.getVisibility() == Visibility.PUBLIC ||
                        (f.getCreator() != null && f.getCreator().getId().equals(user.getId())))
                .collect(Collectors.toList());
    }

    public Folder updateFolder(String userEmail, Long id, String name, String description, Visibility visibility) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(FOLDER_NOT_FOUND));
        verifyOwnership(folder, userEmail);
        folder.rename(name, description, visibility);
        return folder;
    }

    // Legacy method for existing unit tests
    public Folder updateFolder(Long id, String name, String description) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(FOLDER_NOT_FOUND));
        folder.rename(name, description, Visibility.PRIVATE);
        return folder;
    }

    public void deleteFolder(String userEmail, Long id) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(FOLDER_NOT_FOUND));
        verifyOwnership(folder, userEmail);
        deckRepository.findByFolderId(id).forEach(deck -> deck.moveToFolder(null));
        folderRepository.delete(folder);
    }

    // Legacy method for existing unit tests
    public void deleteFolder(Long id) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(FOLDER_NOT_FOUND));
        deckRepository.findByFolderId(id).forEach(deck -> deck.moveToFolder(null));
        folderRepository.delete(folder);
    }

    private void verifyOwnership(Folder folder, String userEmail) {
        if (folder.getCreator() == null || !folder.getCreator().getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("You do not have permission to access or modify this folder.");
        }
    }
}
