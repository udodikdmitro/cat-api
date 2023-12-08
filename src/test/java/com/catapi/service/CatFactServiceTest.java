package com.catapi.service;

import com.catapi.exception.ExternalApiException;
import com.catapi.jpa.CatFactRepository;
import com.catapi.view.CatFactResponse;
import com.catapi.view.CatFactView;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.List;
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
    void getCatFactsFromApi_should_return_list_of_CatFactView_when_response_is_not_null(){
        CatFactResponse firstCatFactResponse = new CatFactResponse();
        firstCatFactResponse.setLastPage(1);
        when(restTemplate.getForObject(eq(CAT_API_URL), any())).thenReturn(firstCatFactResponse);
        CatFactResponse responseWithData = new CatFactResponse();
        responseWithData.setLastPage(1);
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
    void getCatFactsFromApi_should_throw_exception_with_right_message(){
        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new Exception("Помилка"));
        ExternalApiException e = assertThrows(ExternalApiException.class, () -> catFactService.getCatFactsFromApi());
        assertEquals("Cannot get cat fact from external api: Помилка", e.getMessage());
    }

    @Test
    void saveNewFactsFromExternalApi_should_save_only_new_cat_facts() {
        CatFactResponse firstCatFactResponse = new CatFactResponse();
        firstCatFactResponse.setLastPage(1);
        when(restTemplate.getForObject(eq(CAT_API_URL), any())).thenReturn(firstCatFactResponse);
        CatFactResponse responseWithData = new CatFactResponse();
        responseWithData.setLastPage(1);
        List<CatFactView> data = List.of(
                new CatFactView("New fact 1", 9),
                new CatFactView("New fact 2", 9),
                new CatFactView("Existing fact", 13)
        );
        responseWithData.setData(data);
        when(restTemplate.getForObject(eq(CAT_API_URL + "?page=1"), any()))
                .thenReturn(responseWithData);

        Set<String> dbFacts = new HashSet<>();
        dbFacts.add("Existing fact");

        when(catFactRepository.getAllFacts()).thenReturn(dbFacts);
        catFactService.saveNewFactsFromExternalApi();
        verify(catFactRepository, times(2)).save(any());
    }


    @Test
    void getNumberOfFacts_returns_count_of_facts_in_table() {
        when(catFactRepository.count()).thenReturn(3L);
        long result = catFactService.getNumberOfFacts();
        assertEquals(3L, result);
    }
}