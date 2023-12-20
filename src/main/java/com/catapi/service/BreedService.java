package com.catapi.service;

import com.catapi.entity.Breed;
import com.catapi.jpa.BreedRepository;
import com.catapi.view.BreedView;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class BreedService {
    public static final String BREED_API_URL =
            "https://api.thecatapi.com/v1/breeds";
    private final RestTemplate restTemplate;
    private final BreedRepository breedRepository;

    public BreedService(RestTemplate restTemplate, BreedRepository breedRepository) {
        this.restTemplate = restTemplate;
        this.breedRepository = breedRepository;
    }

    public List<BreedView> getAllBreedsFromExternalApi() {
        ResponseEntity<List<BreedView>> responseEntity = restTemplate.exchange(BREED_API_URL, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<BreedView>>() {}
        );
        List<BreedView> catBreedsList = responseEntity.getBody();
        return Objects.requireNonNullElseGet(catBreedsList, List::of);
    }

    public void updateBreedDb() {
        List<BreedView> externalBreedViews = getAllBreedsFromExternalApi();

        for (BreedView breedView : externalBreedViews) {
            String id = breedView.id();
            Optional<Breed> optionalEntity = breedRepository.findByOuterBreedId(id);//t

            optionalEntity.ifPresentOrElse(
                    existingEntity ->
                        updateBreed(breedView, existingEntity, id),
                    () ->
                        saveNewBreed(breedView, id));
            // подивитися, як це можна зробити за допомогою ifPresentOrElse
            // зробити тести:
            // зробити два методи - оновлення і додавання  і написати на них тести
        }
    }

    private void updateBreed(BreedView breedView, Breed existingEntity, String id) {
        existingEntity.setOuterBreedId(id);
        existingEntity.setBreedName(breedView.name());
        existingEntity.setDescription(breedView.description());
        breedRepository.save(existingEntity);
    }

    private void saveNewBreed(BreedView breedView, String id) {
        Breed newBreed = new Breed();
        updateBreed(breedView, newBreed, id);
    }
}