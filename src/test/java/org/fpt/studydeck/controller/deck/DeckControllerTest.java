package org.fpt.studydeck.controller.deck;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.fpt.studydeck.service.deck.DeckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.fpt.studydeck.repository.auth.AppUserRepository;
import org.fpt.studydeck.domain.auth.AppUser;
import org.fpt.studydeck.domain.deck.Visibility;
import org.junit.jupiter.api.BeforeEach;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.security.test.context.support.WithMockUser
class DeckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeckService deckService;

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
    void createsDeck() throws Exception {
        mockMvc.perform(post("/api/v1/decks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Korean Basics\",\"description\":\"Starter words\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Korean Basics"));
    }

    @Test
    void returnsNotFoundWhenRemovingDeckFromMissingFolder() throws Exception {
        var deck = deckService.createDeck("user", null, "Korean Basics", null, Visibility.PUBLIC);

        mockMvc.perform(delete("/api/v1/folders/{folderId}/decks/{deckId}", 999L, deck.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Folder was not found."));
    }
}
