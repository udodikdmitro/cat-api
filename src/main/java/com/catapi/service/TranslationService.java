package com.catapi.service;

import com.catapi.entity.CatFact;
import com.catapi.entity.CatFactTranslation;
import com.catapi.enums.ActiveState;
import com.catapi.enums.Locale;
import com.catapi.enums.UpdateMode;
import com.catapi.exception.ExternalApiException;
import com.catapi.exception.TranslationException;
import com.catapi.jpa.CatFactRepository;
import com.catapi.jpa.CatFactTranslationRepository;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
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

    public TranslationService(
            CatFactTranslationRepository catFactTranslationRepository,
            CatFactRepository catFactRepository, OkHttpClient okHttpClient) {
        this.catFactTranslationRepository = catFactTranslationRepository;
        this.catFactRepository = catFactRepository;
        this.okHttpClient = okHttpClient;
    }

    public String translate(Locale locale, String text) {
        return getLinguatoolsTranslation(locale, text);
    }

    public void translateAllByLinguatools(Locale locale) {
        final List<CatFact> allFacts = catFactRepository.findAllByActiveState(ActiveState.ACTIVE);
        allFacts.forEach(fact -> {
            Optional<CatFactTranslation> localeTranslation = fact.getCatFactTranslation().stream()
                    .filter(catFactTranslation -> catFactTranslation.getLocale() == locale)
                    .findFirst();

            localeTranslation.ifPresentOrElse(
                    existingTranslation -> log.debug("Translation is already present. No need to translate"),
                    () -> {
                        String textToTranslate = fact.getFact();
                        String translationText = getLinguatoolsTranslation(locale, textToTranslate);
                        createCatFactTranslation(fact, locale, translationText);
                    });
        });
        log.info("All active facts without translations to " + locale + " are translated");
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
                return foundText;
            }
        }
        throw new ExternalApiException("Wrong response format");
    }
}