package org.fpt.studydeck.controller.deck;

import java.util.List;

import jakarta.validation.Valid;
import org.fpt.studydeck.dto.deck.CreateFolderRequest;
import org.fpt.studydeck.dto.deck.FolderResponse;
import org.fpt.studydeck.dto.deck.UpdateFolderRequest;
import org.fpt.studydeck.service.deck.FolderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/folders")
public class FolderController {

    private final FolderService folderService;

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @GetMapping
    public List<FolderResponse> listFolders() {
        return folderService.listFolders().stream()
                .map(FolderResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FolderResponse createFolder(@Valid @RequestBody CreateFolderRequest request) {
        return FolderResponse.from(folderService.createFolder(request.name(), request.description()));
    }

    @GetMapping("/{folderId}")
    public FolderResponse getFolder(@PathVariable("folderId") Long folderId) {
        return FolderResponse.from(folderService.getFolder(folderId));
    }

    @PatchMapping("/{folderId}")
    public FolderResponse updateFolder(
            @PathVariable("folderId") Long folderId,
            @Valid @RequestBody UpdateFolderRequest request) {
        return FolderResponse.from(folderService.updateFolder(folderId, request.name(), request.description()));
    }

    @DeleteMapping("/{folderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFolder(@PathVariable("folderId") Long folderId) {
        folderService.deleteFolder(folderId);
    }
}
