package com.catapi.service;

import com.catapi.entity.Breed;
import com.catapi.exception.ExternalApiException;
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
        List<BreedView> catBreedsList = requestBreeds().getBody();
        return Objects.requireNonNullElseGet(catBreedsList, List::of);
    }

    private ResponseEntity<List<BreedView>> requestBreeds(){
        try{
            return restTemplate.exchange(BREED_API_URL, HttpMethod.GET, null,
                    new ParameterizedTypeReference<>(){});
        } catch (Exception e){
            throw new ExternalApiException("Cannot get breed from external api: " + e.getMessage());
        }
    }

    public void updateBreedDb() {
        List<BreedView> externalBreedViews = getAllBreedsFromExternalApi();

        for (BreedView breedView : externalBreedViews) {
            String id = breedView.id();
            Optional<Breed> optionalEntity = breedRepository.findByOuterBreedId(id);

            optionalEntity.ifPresentOrElse(
                    existingEntity -> updateBreed(breedView, existingEntity),
                    () -> saveNewBreed(breedView));
        }
    }

    private void updateBreed(BreedView breedView, Breed existingEntity) {
        String id = breedView.id();
        existingEntity.setOuterBreedId(id);
        existingEntity.setBreedName(breedView.name());
        existingEntity.setDescription(breedView.description());
        breedRepository.save(existingEntity);
    }

    private void saveNewBreed(BreedView breedView) {
        Breed newBreed = new Breed();
        newBreed.setOuterBreedId(breedView.id());
        newBreed.setBreedName(breedView.name());
        newBreed.setDescription(breedView.description());
        breedRepository.save(newBreed);
    }
}