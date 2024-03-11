package com.catapi.controller;

import com.catapi.entity.BreedTranslation;
import com.catapi.entity.CatFactTranslation;
import com.catapi.enums.Locale;
import com.catapi.exceptionhandler.ApiErrorHandler;
import com.catapi.jpa.BreedTranslationRepository;
import com.catapi.jpa.CatFactTranslationRepository;
import com.catapi.service.TranslationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TranslationControllerTest {
    private MockMvc mockMvc;
    private final CatFactTranslationRepository catFactTranslationRepository = mock();
    private final BreedTranslationRepository breedTranslationRepository = mock();

    private final TranslationService translationService = mock();

    private final TranslationController translationController = new TranslationController(
            translationService,
            catFactTranslationRepository,
            breedTranslationRepository
    );

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(translationController)
                .setControllerAdvice(new ApiErrorHandler())
                .build();
    }

    @Test
    void translateAllCatFactsByLinguatools_should_return_ok_status() throws Exception {
        mockMvc.perform(post("/translations/linguatools/translateAllCatFacts")
                        .param("locale", Locale.UK.name()))
                .andExpect(status().isOk());
    }

    @Test
    void updateCatFactTranslationManually_should_return_ok_status() throws Exception {
        CatFactTranslation catFactTranslation = new CatFactTranslation();
        catFactTranslation.setId(1L);
        when(catFactTranslationRepository.findById(any())).thenReturn(Optional.of(catFactTranslation));
        mockMvc.perform(put("/translations/catFactTranslation/{catFactTranslationId}", 1L)
                        .param("newTranslationText", "New translation"))
                .andExpect(status().isOk());
    }

    @Test
    void updateCatFactTranslationManually_should_return_notFound_status_if_entity_is_not_found() throws Exception {
        mockMvc.perform(put("/translations/catFactTranslation/{catFactTranslationId}", 1L)
                        .param("newTranslationText", "New translation"))
                .andExpect(status().isNotFound());
    }

    @Test
    void translateTest_should_return_ok_status_and_translated_text() throws Exception {
        String translatedText = "Translated text";
        when(translationService.translate(Locale.UK, "Text to translate")).thenReturn(translatedText);

        mockMvc.perform(get("/translations/linguatools/translator")
                        .param("textToTranslate", "Text to translate")
                        .param("locale", Locale.UK.name()))
                .andExpect(status().isOk())
                .andExpect(content().string(translatedText));
    }

    @Test
    void translateAllBreedsByLinguatools_should_return_ok_status() throws Exception {
        mockMvc.perform(post("/translations/linguatools/translateAllBreeds")
                        .param("locale", Locale.RU.name()))
                .andExpect(status().isOk());
    }

    @Test
    void updateBreedTranslationManually_should_return_ok_status() throws Exception {
        BreedTranslation breedTranslation = new BreedTranslation();
        breedTranslation.setId(1L);
        when(breedTranslationRepository.findById(any())).thenReturn(Optional.of(breedTranslation));
        String requestBody = "{\"newBreedName\":\"SomeName\",\"newDescription\":\"SomeDescription\"}";
        mockMvc.perform(put("/translations/breedTranslation/{breedTranslationId}", 1L)
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void updateBreedTranslationManually_should_return_badRequest_if_required_field_is_absent() throws Exception {
        String requestBody = "{\"newBreedName\":\"SomeName\"}";
        mockMvc.perform(put("/translations/breedTranslation/{breedTranslationId}", 1L)
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}