package com.catapi.controller;

import com.catapi.service.BreedService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

class BreedControllerTest {

    @InjectMocks
    private BreedController breedController;

    @Mock
    private BreedService breedService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUpdateBreedsFromExternalApiController() {
        ResponseEntity<String> response = breedController.updateBreedsFromExternalApiController();

        verify(breedService).updateBreedsFromExternalApi();
        assertEquals("Breeds are updated", response.getBody());
    }

}

