package com.catapi.controller;

import com.catapi.entity.CatFactTranslation;
import com.catapi.enums.Locale;
import com.catapi.jpa.CatFactTranslationRepository;
import com.catapi.service.TranslationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;

@RestController
@RequestMapping("/translations")
public class TranslationController {
    private final TranslationService translationService;
    private final CatFactTranslationRepository catFactTranslationRepository;

    public TranslationController(
            TranslationService translationService,
            CatFactTranslationRepository catFactTranslationRepository) {
        this.translationService = translationService;
        this.catFactTranslationRepository = catFactTranslationRepository;
    }

    @PostMapping("/linguatools/translateAllCatFacts")
    public void translateAllCatFactsByLinguatools(@RequestParam Locale locale) {
        translationService.translateAllCatFactsByLinguatools(locale);
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

    @PostMapping("/linguatools/translateAllBreeds")
    public ResponseEntity<String> translateAllBreedsByLinguatools(@RequestParam Locale locale) {
        try {
            if (EnumSet.of(Locale.UK, Locale.RU).contains(Locale.valueOf(locale.toString()))) {
                return ResponseEntity.ok(translationService.translateAllBreedsByLinguatools(locale));
            } else {
                throw new IllegalArgumentException(STR."Can not get translation with using parameter \{locale}");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Can not get translation with using this parameter locale");
    }
}