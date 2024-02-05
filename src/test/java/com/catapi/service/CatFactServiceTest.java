package com.catapi.service;

import com.catapi.entity.CatFact;
import com.catapi.enums.ActiveState;
import com.catapi.exception.ExternalApiException;
import com.catapi.jpa.CatFactRepository;
import com.catapi.view.CatFactDataResponse;
import com.catapi.view.CatFactLastPageResponse;
import com.catapi.view.CatFactUpdateView;
import com.catapi.view.CatFactView;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.catapi.service.CatFactService.CAT_API_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CatFactServiceTest {

    RestTemplate restTemplate = Mockito.mock();
    CatFactRepository catFactRepository = Mockito.mock();
    CatFactService catFactService = new CatFactService(restTemplate, catFactRepository);

    @Test
    void getCatFactsFromApi_should_return_empty_list_when_response_is_null() {
        when(restTemplate.getForObject(anyString(), any())).thenReturn(null);
        List<CatFactView> result = catFactService.getCatFactsFromApi();
        List<CatFactView> expected = List.of();
        assertEquals(expected, result);
    }

    @Test
    void getCatFactsFromApi_should_return_list_of_CatFactView_when_response_is_not_null() {
        CatFactLastPageResponse firstCatFactLastPageResponse = new CatFactLastPageResponse();
        firstCatFactLastPageResponse.setLastPage(1);
        when(restTemplate.getForObject(eq(CAT_API_URL), any())).thenReturn(firstCatFactLastPageResponse);
        CatFactDataResponse responseWithData = new CatFactDataResponse();
        List<CatFactView> data = List.of(
                new CatFactView("Fact 1", 5),
                new CatFactView("Fact 2", 5)
        );
        responseWithData.setData(data);
        when(restTemplate.getForObject(eq(CAT_API_URL + "?page=1"), any()))
                .thenReturn(responseWithData);
        List<CatFactView> result = catFactService.getCatFactsFromApi();
        List<CatFactView> expected = List.of(
                new CatFactView("Fact 1", 5),
                new CatFactView("Fact 2", 5)
        );
        assertEquals(expected, result);
    }

    @Test
    void getCatFactsFromApi_should_throw_exception_with_right_message() {
        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new RuntimeException("Помилка"));
        ExternalApiException e = assertThrows(ExternalApiException.class, () -> catFactService.getCatFactsFromApi());
        assertEquals("Cannot get number of pages from external api: Помилка", e.getMessage());
    }

    @Test
    void getCatFactsFromApi_should_throw_ExternalApiException_with_right_message(){
        String errorText = "Some error text";
        CatFactLastPageResponse firstCatFactLastPageResponse = new CatFactLastPageResponse();
        firstCatFactLastPageResponse.setLastPage(1);
        when(restTemplate.getForObject(eq(CAT_API_URL), any())).thenReturn(firstCatFactLastPageResponse);
        when(restTemplate.getForObject(eq(CAT_API_URL + "?page=1"), any())).thenThrow(new RuntimeException(errorText));
        ExternalApiException result = assertThrows(ExternalApiException.class, () -> catFactService.getCatFactsFromApi());
        String expected = "Cannot get cat fact from external api: " + errorText;
        assertEquals(expected, result.getMessage());
    }

    @Test
    void saveNewFactsFromExternalApi_should_save_only_new_cat_facts() {
        CatFactLastPageResponse firstCatFactLastPageResponse = new CatFactLastPageResponse();
        firstCatFactLastPageResponse.setLastPage(1);
        when(restTemplate.getForObject(eq(CAT_API_URL), any())).thenReturn(firstCatFactLastPageResponse);
        List<CatFactView> data = List.of(
                new CatFactView("New fact 1", 9),
                new CatFactView("New fact 2", 9),
                new CatFactView("Existing fact", 13)
        );
        CatFactDataResponse catFactDataResponse = new CatFactDataResponse();
        catFactDataResponse.setData(data);
        when(restTemplate.getForObject(eq(CAT_API_URL + "?page=1"), any()))
                .thenReturn(catFactDataResponse);

        Set<String> dbFacts = new HashSet<>();
        dbFacts.add("Existing fact");

        when(catFactRepository.getAllTextOfFacts()).thenReturn(dbFacts);
        catFactService.saveNewFactsFromExternalApi();
        verify(catFactRepository, times(2)).save(any());
    }


    @Test
    void getNumberOfFacts_returns_count_of_facts_in_table() {
        when(catFactRepository.count()).thenReturn(3L);
        long result = catFactService.getNumberOfFacts();
        assertEquals(3L, result);
    }

    @Test
    void getCatFactsFromApi_should_work_correct_if_response_is_null() {
        CatFactLastPageResponse firstCatFactLastPageResponse = new CatFactLastPageResponse();
        firstCatFactLastPageResponse.setLastPage(2);
        when(restTemplate.getForObject(eq(CAT_API_URL), any())).thenReturn(firstCatFactLastPageResponse);
        CatFactDataResponse responseWithoutData = new CatFactDataResponse();
        responseWithoutData.setData(null);
        when(restTemplate.getForObject(eq(CAT_API_URL + "?page=1"), any()))
                .thenReturn(responseWithoutData);
        CatFactDataResponse responseWithData = new CatFactDataResponse();
        List<CatFactView> data = List.of(
                new CatFactView("New fact 1", 9),
                new CatFactView("New fact 2", 9),
                new CatFactView("New fact 3", 9)
        );
        responseWithData.setData(data);
        when(restTemplate.getForObject(eq(CAT_API_URL + "?page=2"), any()))
                .thenReturn(responseWithData);
        List<CatFactView> result = catFactService.getCatFactsFromApi();
        List<CatFactView> expected = List.of(
                new CatFactView("New fact 1", 9),
                new CatFactView("New fact 2", 9),
                new CatFactView("New fact 3", 9)
        );
        assertEquals(expected, result);
    }

    @Test
    void updateCatFact_should_update_cat_fact_text() {
        Long id = 1L;
        String newFactText = "Updated cat fact";
        CatFact catFact = new CatFact();
        catFact.setId(id);
        catFact.setFact("Old cat fact");
        catFact.setActiveState(ActiveState.ACTIVE);

        when(catFactRepository.findById(id)).thenReturn(Optional.of(catFact));

        CatFactUpdateView updateView = mock(CatFactUpdateView.class);
        when(updateView.newFactText()).thenReturn(newFactText);

        catFactService.updateCatFact(id, updateView);

        ArgumentCaptor<CatFact> captor = ArgumentCaptor.forClass(CatFact.class);
        verify(catFactRepository).save(captor.capture());

        CatFact updatedCatFact = captor.getValue();

        String expectedSavedText = "Updated cat fact";
        assertEquals(expectedSavedText, updatedCatFact.getFact());
    }

    @Test
    void updateCatFact_should_throw_EntityNotFoundException_if_cat_fact_is_not_found() {
        Long id = 1L;
        when(catFactRepository.findById(id)).thenReturn(Optional.empty());

        CatFactUpdateView updateView = mock(CatFactUpdateView.class);
        assertThrows(EntityNotFoundException.class, () -> catFactService.updateCatFact(id, updateView));
    }

    @Test
    void updateCatFactState_should_update_cat_fact_ActiveState() {
        Long id = 1L;
        ActiveState newState = ActiveState.NOT_ACTIVE;
        CatFact catFact = new CatFact();
        catFact.setId(id);
        catFact.setFact("Some cat fact");
        catFact.setActiveState(ActiveState.ACTIVE);

        when(catFactRepository.findById(id)).thenReturn(Optional.of(catFact));

        catFactService.updateCatFactState(id, newState);

        ArgumentCaptor<CatFact> captor = ArgumentCaptor.forClass(CatFact.class);
        verify(catFactRepository).save(captor.capture());

        CatFact updatedCatFact = captor.getValue();
        assertEquals(ActiveState.NOT_ACTIVE, updatedCatFact.getActiveState());
    }

    @Test
    void updateCatFactState_should_throw_EntityNotFoundException_if_cat_fact_is_not_found() {
        Long id = 1L;
        when(catFactRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> catFactService.updateCatFactState(id, ActiveState.NOT_ACTIVE));
    }
}