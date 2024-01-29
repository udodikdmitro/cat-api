package com.catapi.controller;

import com.catapi.entity.CatFactTranslation;
import com.catapi.enums.Locale;
import com.catapi.jpa.CatFactTranslationRepository;
import com.catapi.service.TranslationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/translations")
public class CatFactTranslationController {
    private final TranslationService translationService;
    private final CatFactTranslationRepository catFactTranslationRepository;

    public CatFactTranslationController(
            TranslationService translationService,
            CatFactTranslationRepository catFactTranslationRepository) {
        this.translationService = translationService;
        this.catFactTranslationRepository = catFactTranslationRepository;
    }

    @PostMapping("/linguatools/catFacts")
    public void translateAllCatFactsByLinguatools(@RequestParam Locale locale) {
        translationService.translateAllByLinguatools(locale);
    }

    @PutMapping("/catFactTranslation/{catFactTranslationId}")
    public void updateTranslationManually(@PathVariable Long catFactTranslationId, @RequestParam String newTranslationText) {
        CatFactTranslation factTranslationToUpdate = catFactTranslationRepository.findById(catFactTranslationId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "There's no cat fact translation with id + " + catFactTranslationId)
                );
        translationService.updateCatFactTranslation(factTranslationToUpdate, newTranslationText);
    }

    @GetMapping("/linguatools/translator")
    public String translateTest(@RequestParam String textToTranslate, @RequestParam Locale locale) {
        return translationService.translate(locale, textToTranslate);
    }
}