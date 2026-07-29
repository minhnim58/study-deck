package org.fpt.studydeck.domain.deck;

import java.time.Instant;

import org.hibernate.annotations.Formula;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.fpt.studydeck.domain.auth.AppUser;

@Entity
@Table(name = "decks")
public class Deck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private Folder folder;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Visibility visibility = Visibility.PRIVATE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private AppUser creator;

    @Formula("(SELECT COUNT(*) FROM flashcards f WHERE f.deck_id = id)")
    private int totalCards;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Deck() {
    }

    public static Deck create(Folder folder, String title, String description, Visibility visibility, AppUser creator) {
        Deck deck = new Deck();
        deck.folder = folder;
        deck.title = requireTitle(title);
        deck.description = Folder.trimToNull(description);
        deck.visibility = visibility != null ? visibility : Visibility.PRIVATE;
        deck.creator = creator;
        deck.createdAt = Instant.now();
        deck.updatedAt = deck.createdAt;
        return deck;
    }

    public void update(String title, String description, Visibility visibility) {
        this.title = requireTitle(title);
        this.description = Folder.trimToNull(description);
        this.visibility = visibility != null ? visibility : Visibility.PRIVATE;
        this.updatedAt = Instant.now();
    }

    public void moveToFolder(Folder folder) {
        this.folder = folder;
        this.updatedAt = Instant.now();
    }

    private static String requireTitle(String title) {
        String trimmed = Folder.trimToNull(title);
        if (trimmed == null) {
            throw new IllegalArgumentException("Deck title is required.");
        }
        return trimmed;
    }

    public Long getId() {
        return id;
    }

    public Folder getFolder() {
        return folder;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public AppUser getCreator() {
        return creator;
    }

    public int getTotalCards() {
        return totalCards;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
