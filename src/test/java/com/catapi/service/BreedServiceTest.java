package com.catapi.service;

import com.catapi.entity.Breed;
import com.catapi.exception.ExternalApiException;
import com.catapi.jpa.BreedRepository;
import com.catapi.view.BreedView;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BreedServiceTest {
    RestTemplate restTemplate = mock();
    BreedRepository breedRepository = mock();
    BreedService breedService = new BreedService(restTemplate, breedRepository);

    @Test
    void getAllBreedsFromExternalApi_should_return_list_of_BreedViews_if_body_of_responseEntity_is_not_null() {
        List<BreedView> breedViewList = List.of(
                new BreedView("1", "breedView 1", "origin 1", "description 1", "url 1"),
                new BreedView("2", "breedView 2", "origin 2", "description 2", "url 2"),
                new BreedView("3", "breedView 3", "origin 3", "description 3", "url 3"));

        ResponseEntity<List<BreedView>> responseEntity = ResponseEntity.ok(breedViewList);
        when(restTemplate.exchange(anyString(), any(), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        List<BreedView> resultBreedViewList = breedService.getAllBreedsFromExternalApi();
        List<BreedView> expected = List.of(
                new BreedView("1", "breedView 1", "origin 1", "description 1", "url 1"),
                new BreedView("2", "breedView 2", "origin 2", "description 2", "url 2"),
                new BreedView("3", "breedView 3", "origin 3", "description 3", "url 3"));

        assertEquals(expected, resultBreedViewList);
    }

    @Test
    void getAllBreedsFromExternalApi_should_return_list_of_BreedViews_if_body_of_responseEntity_is_null() {
        List<BreedView> breedViewList = List.of();
        ResponseEntity<List<BreedView>> responseEntity = ResponseEntity.ok(breedViewList);

        when(restTemplate.exchange(anyString(), any(), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        List<BreedView> resultBreedViewList = breedService.getAllBreedsFromExternalApi();
        List<BreedView> expected = List.of();

        assertEquals(expected, resultBreedViewList);
    }

    @Test
    void requestBreeds_should_throw_exception_with_right_message() {
        when(restTemplate.exchange(anyString(), any(), isNull(), any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("Помилка"));
        ExternalApiException e = assertThrows(ExternalApiException.class, () -> breedService.getAllBreedsFromExternalApi());
        assertEquals("Cannot get breed from external api: Помилка", e.getMessage());
    }


    @Test
    void updateBreedDb_should_update_existing_breeds_and_add_new_ones() {
        BreedView breedView1 = new BreedView("1", "breedView 1", "origin 1",
                "description 1", "url 1");
        BreedView breedView2 = new BreedView("2", "breedView 2", "origin 2",
                "description 2", "url 2");
        List<BreedView> externalBreedViews = List.of(breedView1, breedView2);

        ResponseEntity<List<BreedView>> responseEntity = ResponseEntity.ok(externalBreedViews);
        when(restTemplate.exchange(anyString(), any(), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);
        Breed updateBreedEntity = new Breed();
        updateBreedEntity.setOuterBreedId("1");

        when(breedRepository.findByOuterBreedId("1")).thenReturn(Optional.of(updateBreedEntity));
        when(breedRepository.findByOuterBreedId("2")).thenReturn(Optional.empty());
        breedService.updateBreedsFromExternalApi();

        ArgumentCaptor<Breed> breedCaptor = ArgumentCaptor.forClass(Breed.class);
        Mockito.verify(breedRepository, Mockito.times(2)).save(breedCaptor.capture());
        List<Breed> arguments = breedCaptor.getAllValues();

        Breed breed1 = new Breed();
        breed1.setOuterBreedId("1");
        breed1.setBreedName("breedView 1");
        breed1.setDescription("description 1");

        Breed breed2 = new Breed();
        breed2.setOuterBreedId("2");
        breed2.setBreedName("breedView 2");
        breed2.setDescription("description 2");

        assertEquals(breed1, arguments.get(0));
        assertEquals(breed2, arguments.get(1));
    }
}