package com.catapi.service;

import com.catapi.entity.Breed;
import com.catapi.entity.BreedTranslation;
import com.catapi.entity.CatFact;
import com.catapi.entity.CatFactTranslation;
import com.catapi.enums.ActiveState;
import com.catapi.enums.Locale;
import com.catapi.enums.UpdateMode;
import com.catapi.exception.ExternalApiException;
import com.catapi.exception.TranslationException;
import com.catapi.jpa.BreedRepository;
import com.catapi.jpa.BreedTranslationRepository;
import com.catapi.jpa.CatFactRepository;
import com.catapi.jpa.CatFactTranslationRepository;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class TranslationService {

    public static final String LINGUATOOLS_URL = "https://linguatools.net/translators/translation_startseite";
    private final CatFactTranslationRepository catFactTranslationRepository;
    private final CatFactRepository catFactRepository;
    private final OkHttpClient okHttpClient;
    private final BreedTranslationRepository breedTranslationRepository;
    private final BreedRepository breedRepository;

    public TranslationService(
            CatFactTranslationRepository catFactTranslationRepository,
            CatFactRepository catFactRepository, OkHttpClient okHttpClient,
            BreedTranslationRepository breedTranslationRepository, BreedRepository breedRepository) {
        this.catFactTranslationRepository = catFactTranslationRepository;
        this.catFactRepository = catFactRepository;
        this.okHttpClient = okHttpClient;
        this.breedTranslationRepository = breedTranslationRepository;
        this.breedRepository = breedRepository;
    }

    public String translate(Locale locale, String text) {
        return getLinguatoolsTranslation(locale, text);
    }

    public void translateAllCatFactsByLinguatools(Locale locale) {
        final List<CatFact> allFacts = catFactRepository.findAllByActiveState(ActiveState.ACTIVE);
        allFacts.forEach(fact -> {
            Optional<CatFactTranslation> localeTranslation = fact.getCatFactTranslations().stream()
                    .filter(catFactTranslation -> catFactTranslation.getLocale() == locale)
                    .findFirst();

            if (localeTranslation.isEmpty()) {
                String textToTranslate = fact.getFact();
                String translationText = getLinguatoolsTranslation(locale, textToTranslate);
                createCatFactTranslation(fact, locale, translationText);
                log.debug("New translation is saved");
            }

        });
        log.info(STR."All active facts without translations to \{locale.getLanguageName()} are translated");
    }

    public void createCatFactTranslation(CatFact catFact, Locale locale, String translationText) {
        CatFactTranslation catFactTranslation = new CatFactTranslation();
        catFactTranslation.setCatFact(catFact);
        catFactTranslation.setLocale(locale);
        catFactTranslation.setTranslationText(translationText);
        catFactTranslation.setUpdateMode(UpdateMode.LINGUA);
        catFactTranslationRepository.save(catFactTranslation);
    }

    public void updateCatFactTranslation(CatFactTranslation factTranslationToUpdate, String newTranslationText) {
        factTranslationToUpdate.setTranslationText(newTranslationText);
        factTranslationToUpdate.setUpdateMode(UpdateMode.MANUAL);
        catFactTranslationRepository.save(factTranslationToUpdate);
    }

    private String getLinguatoolsTranslation(Locale locale, String textToTranslate) {
        final URI uri = UriComponentsBuilder.fromHttpUrl(LINGUATOOLS_URL)
                .queryParam("q", textToTranslate)
                .queryParam("google_source", "en")
                .queryParam("google_target", locale)
                .build().toUri();

        final MediaType mediaType = MediaType.parse("text/javascript; charset=utf-8");
        final RequestBody reqBody = RequestBody.create("Accept", mediaType);
        final Request request = new Request.Builder()
                .url(uri.toString())
                .post(reqBody)
                .addHeader("Accept", "text/javascript; charset=utf-8")
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            final String responseText = Optional.ofNullable(response.body())
                    .map(this::getStringFromRespBody)
                    .orElseThrow(() -> new ExternalApiException("Response body cannot be null"));
            return extractTranslation(responseText);
        } catch (Exception e) {
            throw new TranslationException(e.getMessage());
        }
    }

    private String getStringFromRespBody(ResponseBody respBody) {
        try {
            return respBody.string();
        } catch (IOException e) {
            throw new ExternalApiException(e.getMessage());
        }
    }

    private String extractTranslation(String scriptContent) {
        final String pattern = "\\$\\('#trans_output_content'\\)\\.html\\(\"(.*?)\"\\);";
        final Pattern r = Pattern.compile(pattern);
        final Matcher m = r.matcher(scriptContent);

        while (m.find()) {
            String foundText = m.group(1);
            if (!foundText.isEmpty()) {
                return foundText.replace("&amp;#39;", "ʼ");
            }
        }
        throw new ExternalApiException("Wrong response format");
    }

    public void translateAllBreedsByLinguatools(Locale locale) {
        final List<Breed> allBreeds = breedRepository.findAll();
        allBreeds.forEach(breed -> {
            Optional<BreedTranslation> localeTranslation = breed.getBreedTranslations().stream()
                    .filter(breedTranslation -> breedTranslation.getLocale() == locale)
                    .findFirst();

            if (localeTranslation.isEmpty()) {
                String breedNameToTranslate = breed.getBreedName();
                String descriptionToTranslate = breed.getDescription();
                String textToTranslate =  STR."\{breedNameToTranslate} @@@ \{descriptionToTranslate}";
                String translationText = getLinguatoolsTranslation(locale, textToTranslate);
                createBreedTranslation(breed, locale, translationText);
                log.debug("New translation is saved");
            }
        });
        log.info(STR. "All active breeds without translations to \{ locale.getLanguageName() } are translated");
    }

    public void createBreedTranslation(Breed breed, Locale locale, String translationText) {
        String[] translationParts = translationText.split(" @@@ ");
        String breedName = translationParts[0];
        String description = translationParts[1];

        BreedTranslation breedTranslation = new BreedTranslation();
        breedTranslation.setBreed(breed);
        breedTranslation.setLocale(locale);
        breedTranslation.setBreedName(breedName);
        breedTranslation.setDescription(description);
        breedTranslation.setUpdateMode(UpdateMode.LINGUA);
        breedTranslationRepository.save(breedTranslation);
    }

    public String translateAllBreeds(Locale locale) {
        if (EnumSet.of(Locale.UK, Locale.RU).contains(Locale.valueOf(locale.toString()))) {
            translateAllBreedsByLinguatools(locale);
            return STR. "All active breeds without translations to \{ locale.getLanguageName() } are translated";
        } else {
            log.info(STR."Can not get translation with using parameter \{locale}");   //цього логу немає, не знаю чому
            throw new IllegalArgumentException(STR."Can not get translation with using parameter \{locale}");
        }
    }
}