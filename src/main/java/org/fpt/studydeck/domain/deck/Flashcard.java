package org.fpt.studydeck.domain.deck;

import java.time.Instant;

import jakarta.persistence.*;

@Entity
@Table(name = "flashcards")
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck;

    @Column(nullable = false, length = 255)
    private String term;

    @Column(nullable = false, length = 1000)
    private String definition;

    @Column(name = "term_image_url", length = 2048)
    private String termImageUrl;

    @Column(name = "definition_image_url", length = 2048)
    private String definitionImageUrl;

    @ManyToMany
    @JoinTable(name = "user_starred_flashcards", joinColumns = @JoinColumn(name = "flashcard_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private java.util.Set<org.fpt.studydeck.domain.auth.AppUser> starredByUsers = new java.util.HashSet<>();

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean starred = false;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Flashcard() {
    }

    public static Flashcard create(
            Deck deck,
            String term,
            String definition,
            String termImageUrl,
            String definitionImageUrl,
            int position) {
        if (deck == null) {
            throw new IllegalArgumentException("Deck is required.");
        }
        Flashcard flashcard = new Flashcard();
        flashcard.deck = deck;
        flashcard.term = requireTerm(term);
        flashcard.definition = requireDefinition(definition);
        flashcard.termImageUrl = Folder.trimToNull(termImageUrl);
        flashcard.definitionImageUrl = Folder.trimToNull(definitionImageUrl);
        flashcard.position = position;
        flashcard.createdAt = Instant.now();
        flashcard.updatedAt = flashcard.createdAt;
        return flashcard;
    }

    public void update(String term, String definition, String termImageUrl, String definitionImageUrl) {
        this.term = requireTerm(term);
        this.definition = requireDefinition(definition);
        this.termImageUrl = Folder.trimToNull(termImageUrl);
        this.definitionImageUrl = Folder.trimToNull(definitionImageUrl);
        this.updatedAt = Instant.now();
    }

    public void toggleStar(org.fpt.studydeck.domain.auth.AppUser user, boolean starred) {
        if (starred) {
            this.starredByUsers.add(user);
        } else {
            this.starredByUsers.remove(user);
        }
        this.updatedAt = Instant.now();
    }

    public boolean isStarredBy(String userEmail) {
        return starredByUsers.stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(userEmail));
    }

    private static String requireTerm(String term) {
        String trimmed = Folder.trimToNull(term);
        if (trimmed == null) {
            throw new IllegalArgumentException("Term is required.");
        }
        return trimmed;
    }

    private static String requireDefinition(String definition) {
        String trimmed = Folder.trimToNull(definition);
        if (trimmed == null) {
            throw new IllegalArgumentException("Definition is required.");
        }
        return trimmed;
    }

    public Long getId() {
        return id;
    }

    public Deck getDeck() {
        return deck;
    }

    public String getTerm() {
        return term;
    }

    public String getDefinition() {
        return definition;
    }

    public String getTermImageUrl() {
        return termImageUrl;
    }

    public String getDefinitionImageUrl() {
        return definitionImageUrl;
    }

    public int getPosition() {
        return position;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
