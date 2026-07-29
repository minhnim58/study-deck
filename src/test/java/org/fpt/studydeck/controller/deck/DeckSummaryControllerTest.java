package org.fpt.studydeck.controller.deck;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.fpt.studydeck.service.deck.DeckService;
import org.fpt.studydeck.service.deck.FlashcardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.fpt.studydeck.repository.auth.AppUserRepository;
import org.fpt.studydeck.domain.auth.AppUser;
import org.fpt.studydeck.domain.deck.Visibility;
import org.junit.jupiter.api.BeforeEach;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.security.test.context.support.WithMockUser
class DeckSummaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeckService deckService;

    @Autowired
    private FlashcardService flashcardService;

    @Autowired
    private AppUserRepository appUserRepository;

    @BeforeEach
    void setUp() {
        if (!appUserRepository.existsByEmail("user")) {
            AppUser user = AppUser.create("user", "password", "Test User");
            appUserRepository.save(user);
        }
    }

    @Test
    void getsDeckSummary() throws Exception {
        var deck = deckService.createDeck("user", null, "Korean Basics", null, Visibility.PUBLIC);
        flashcardService.createFlashcard(deck.getId(), "first", "one", null, null);

        mockMvc.perform(get("/api/v1/decks/{deckId}/summary", deck.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deckId").value(deck.getId()))
                .andExpect(jsonPath("$.totalCards").value(1))
                .andExpect(jsonPath("$.newCards").value(1));
    }
}
