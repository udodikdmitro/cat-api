package com.catapi.service;

import com.catapi.entity.Breed;
import com.catapi.entity.BreedTranslation;
import com.catapi.entity.CatFact;
import com.catapi.entity.CatFactTranslation;
import com.catapi.enums.ActiveState;
import com.catapi.enums.Locale;
import com.catapi.enums.UpdateMode;
import com.catapi.exception.TranslationException;
import com.catapi.jpa.BreedRepository;
import com.catapi.jpa.BreedTranslationRepository;
import com.catapi.jpa.CatFactRepository;
import com.catapi.jpa.CatFactTranslationRepository;
import okhttp3.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.catapi.service.TranslationService.LINGUATOOLS_URL;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TranslationServiceTest {
    private final CatFactTranslationRepository catFactTranslationRepository = mock();
    private final CatFactRepository catFactRepository = mock();
    private final OkHttpClient okHttpClient = mock();
    private final BreedTranslationRepository breedTranslationRepository = mock();
    private final BreedRepository breedRepository = mock();
    private final TranslationService translationService = new TranslationService(
            catFactTranslationRepository,
            catFactRepository,
            okHttpClient,
            breedTranslationRepository,
            breedRepository);

    @Test
    void translate_should_return_translation() {
        String textToTranslate = "Translate me";
        Locale locale = Locale.RU;
        String expectedTranslation = "Переведи меня";
        mockOkHttpClientResponse(textToTranslate, expectedTranslation);

        String translation = translationService.translate(locale, textToTranslate);
        assertEquals(expectedTranslation, translation);
    }

    @Test
    void translateAllCatFactsByLinguatools_should_save_only_not_existing_facts() {
        CatFactTranslation catFactTranslation = new CatFactTranslation();
        catFactTranslation.setId(1L);
        catFactTranslation.setLocale(Locale.RU);
        catFactTranslation.setTranslationText("Какой-то текст");
        catFactTranslation.setUpdateMode(UpdateMode.LINGUA);

        CatFact catFact1 = new CatFact();
        catFact1.setId(1L);
        catFact1.setFact("Some text 1");
        catFact1.setCatFactTranslations(List.of(catFactTranslation));
        catFactTranslation.setCatFact(catFact1);
        mockOkHttpClientResponse("Some text 1", "Якийсь текст 1");

        CatFactTranslation catFactTranslation2 = new CatFactTranslation();
        catFactTranslation2.setId(2L);
        catFactTranslation2.setLocale(Locale.UK);
        catFactTranslation2.setTranslationText("Якийсь текст");
        catFactTranslation2.setUpdateMode(UpdateMode.LINGUA);

        CatFact catFact2 = new CatFact();
        catFact2.setId(2L);
        catFact2.setFact("Some text 2");
        catFact2.setCatFactTranslations(List.of(catFactTranslation2));
        catFactTranslation2.setCatFact(catFact2);

        when(catFactRepository.findAllByActiveState(ActiveState.ACTIVE)).thenReturn(List.of(catFact1, catFact2));
        translationService.translateAllCatFactsByLinguatools(Locale.UK);

        ArgumentCaptor<CatFactTranslation> breedCaptor = ArgumentCaptor.forClass(CatFactTranslation.class);
        verify(catFactTranslationRepository, times(1)).save(breedCaptor.capture());

        CatFactTranslation savedTranslation = breedCaptor.getAllValues().getFirst();

        CatFactTranslation expectedSaved = new CatFactTranslation();
        expectedSaved.setLocale(Locale.UK);
        expectedSaved.setTranslationText("Якийсь текст 1");
        expectedSaved.setCatFact(catFact1);
        expectedSaved.setUpdateMode(UpdateMode.LINGUA);

        assertEquals(expectedSaved, savedTranslation);
    }

    @Test
    void translate_should_throw_TranslationException_when_externalApi_fails() throws IOException {
        String textToTranslate = "Translate me";
        Locale locale = Locale.RU;

        Call mockCall = mock(Call.class);
        when(mockCall.execute()).thenThrow(new IOException("Failed to call external API"));

        when(okHttpClient.newCall(any(Request.class))).thenReturn(mockCall);

        TranslationException translationException = assertThrows(
                TranslationException.class,
                () -> translationService.translate(locale, textToTranslate)
        );

        String errorMessage = translationException.getMessage();
        assertEquals("Failed to call external API", errorMessage);
    }

    @Test
    void translate_should_handle_empty_response_from_externalApi() {
        String textToTranslate = "Translate me";
        Locale locale = Locale.RU;
        mockOkHttpClientResponse(textToTranslate, ""); // Предполагается, что пустой ответ означает отсутствие перевода

        TranslationException translationException = assertThrows(
                TranslationException.class,
                () -> translationService.translate(locale, textToTranslate)
        );

        assertEquals("Wrong response format", translationException.getMessage());
    }

    @Test
    void updateCatFactTranslation_should_correctly_update_translation() {
        CatFactTranslation catFactTranslation = new CatFactTranslation();
        catFactTranslation.setId(1L);
        catFactTranslation.setLocale(Locale.RU);
        catFactTranslation.setTranslationText("Исходный текст");
        String newTranslationText = "Обновленный текст";

        translationService.updateCatFactTranslation(catFactTranslation, newTranslationText);

        assertEquals(newTranslationText, catFactTranslation.getTranslationText());
        assertEquals(UpdateMode.MANUAL, catFactTranslation.getUpdateMode());

        ArgumentCaptor<CatFactTranslation> breedCaptor = ArgumentCaptor.forClass(CatFactTranslation.class);
        verify(catFactTranslationRepository).save(breedCaptor.capture());

        CatFactTranslation updatedTranslation = breedCaptor.getAllValues().getFirst();

        CatFactTranslation expectedUpdated = new CatFactTranslation();
        expectedUpdated.setLocale(Locale.RU);
        expectedUpdated.setTranslationText("Обновленный текст");
        expectedUpdated.setUpdateMode(UpdateMode.MANUAL);

        assertEquals(expectedUpdated, updatedTranslation);
    }


    private void mockOkHttpClientResponse(String originalText, String expectedTranslation) {
        ResponseBody responseBody = ResponseBody.create(
                getTranslationResponse(expectedTranslation),
                MediaType.get("text/javascript; charset=utf-8")
        );

        // Создаем объект ответа
        Response response = new Response.Builder()
                .request(new Request.Builder().url(LINGUATOOLS_URL).build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(responseBody)
                .build();

        Call call = mock(Call.class);
        try {
            when(call.execute()).thenReturn(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        when(okHttpClient.newCall(argThat(request -> {
            String query = request.url().toString();
            return URLDecoder.decode(query, StandardCharsets.UTF_8).contains(originalText);
        }))).thenReturn(call);
    }

    private String getTranslationResponse(String testToTranslate) {
        return String.format("""
                \t$('#translate_input').css("visibility","");
                \t$('#translate_input').css("visibility","visible");
                \t//console.log("@trans: text");
                \t\t$('#detected_language').empty();
                \t\t$('#detected_language').removeAttr("code_source");
                \t\t$('#detected_language').removeClass("alert-info -p-2");
                \t$('#trans_output_content').html("");
                \t$('#trans_output_content').html("%s");
                \t$('#translated_by').empty();

                \t$('#lt_trans_header').css("visibility","visible");
                \t$('#lt_trans_l1').empty();

                \t$('#startseite_deeplink').css("visibility","visible");
                \t$('#deeplink').empty();


                \tvar translated_by_name = "Google Translate";
                \tvar translated_by_url = "https://translate.google.com/";
                \t$('#translated_by').html("Translated by <a href=" + translated_by_url + " target='_blank' rel='nofollow' class='text-secondary' style='text-decoration:underline;'>" + translated_by_name + "</a>");
                """, testToTranslate);
    }

    @Test
    void testTranslateAllBreedsByLinguatools_should_save_only_absent_breed_translations_once() {
        BreedTranslation breedTranslation1 = new BreedTranslation();
        breedTranslation1.setBreedName("Порода1");
        breedTranslation1.setDescription("Опис1");
        breedTranslation1.setLocale(Locale.UK);
        Breed breed1 = new Breed();
        breed1.setId(1L);
        breed1.setBreedName("Breed1");
        breed1.setDescription("Description1");
        breed1.setBreedTranslations(List.of(breedTranslation1));

        Breed breed2 = new Breed();
        breed2.setId(2L);
        breed2.setBreedName("Breed2");
        breed2.setDescription("Description2");
        breed2.setBreedTranslations(List.of());

        when(breedRepository.findAll()).thenReturn(List.of(breed1, breed2));

        mockOkHttpClientResponse("Breed2 @@@ Description2", "Порода2 @@@ Опис2");

        translationService.translateAllBreedsByLinguatools(Locale.UK);

        ArgumentCaptor<BreedTranslation> breedCaptor = ArgumentCaptor.forClass(BreedTranslation.class);
        verify(breedTranslationRepository, times(1)).save(breedCaptor.capture());

        BreedTranslation saveBreedTranslation = breedCaptor.getAllValues().getFirst();
        assertEquals("Порода2", saveBreedTranslation.getBreedName());
        assertEquals("Опис2", saveBreedTranslation.getDescription());
    }
}