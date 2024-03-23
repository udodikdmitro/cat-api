package com.catapi.controller;

import com.catapi.entity.BreedTranslation;
import com.catapi.entity.CatFactTranslation;
import com.catapi.enums.Locale;
import com.catapi.jpa.BreedTranslationRepository;
import com.catapi.jpa.CatFactTranslationRepository;
import com.catapi.service.TranslationService;
import com.catapi.view.BreedUpdateTranslationView;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/translations")
public class TranslationController {
    private final TranslationService translationService;
    private final CatFactTranslationRepository catFactTranslationRepository;
    private final BreedTranslationRepository breedTranslationRepository;

    public TranslationController(
            TranslationService translationService,
            CatFactTranslationRepository catFactTranslationRepository, BreedTranslationRepository breedTranslationRepository) {
        this.translationService = translationService;
        this.catFactTranslationRepository = catFactTranslationRepository;
        this.breedTranslationRepository = breedTranslationRepository;
    }

    @PostMapping("/linguatools/translateAllCatFacts")
    public ResponseEntity<Void> translateAllCatFactsByLinguatools(@RequestParam("locale") Locale locale) {
        translationService.translateAllCatFactsByLinguatools(locale);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/catFactTranslation/{catFactTranslationId}")
    public void updateCatFactTranslationManually(@PathVariable Long catFactTranslationId, @RequestParam String newTranslationText) {
        CatFactTranslation factTranslationToUpdate = catFactTranslationRepository.findById(catFactTranslationId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "There's no cat fact translation with id + " + catFactTranslationId)
                );
        translationService.updateCatFactTranslation(factTranslationToUpdate, newTranslationText);
    }

    @GetMapping("/linguatools/translator")
    public String translateTest(@RequestParam String textToTranslate, @RequestParam("locale") Locale locale) {
        return translationService.translate(locale, textToTranslate);
    }

    @PostMapping("/linguatools/translateAllBreeds")
    public ResponseEntity<Void> translateAllBreedsByLinguatools(@RequestParam("locale") Locale locale) {
        translationService.translateAllBreedsByLinguatools(locale);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/breedTranslation/{breedTranslationId}")
    public void updateBreedTranslationManually(
            @PathVariable Long breedTranslationId,
            @RequestBody @Valid BreedUpdateTranslationView breedUpdateTranslationView) {
        BreedTranslation breedTranslationToUpdate = breedTranslationRepository.findById(breedTranslationId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "There's no breed translation with id + " + breedTranslationId)
                );
        translationService.updateBreedTranslation(breedTranslationToUpdate, breedUpdateTranslationView);
    }
}