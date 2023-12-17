package com.catapi.service;

import com.catapi.jpa.CatFactRepository;
import com.catapi.view.BreedView;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;

@Service
public class BreedService {
    public static final String BREED_API_URL =
            "https://api.thecatapi.com/v1/breeds";
    private final RestTemplate restTemplate;
    private final CatFactRepository catFactRepository;

    public BreedService(RestTemplate restTemplate, CatFactRepository catFactRepository) {
        this.restTemplate = restTemplate;
        this.catFactRepository = catFactRepository;
    }

    public List<BreedView> getAllBreedsFromExternalApi() {
        BreedView[] catBreedsArray = restTemplate.getForObject(BREED_API_URL, BreedView[].class);
            return List.of(catBreedsArray);
    }
}