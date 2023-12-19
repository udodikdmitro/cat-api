package com.catapi.service;

import com.catapi.entity.Breed;
import com.catapi.jpa.BreedRepository;
import com.catapi.view.BreedView;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
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
        BreedView[] catBreedsArray = restTemplate.getForObject(BREED_API_URL, BreedView[].class);
        assert catBreedsArray != null;
        return List.of(catBreedsArray);
    }

    public String updateBreedDb(){
        List<BreedView> externalBreedView = getAllBreedsFromExternalApi();

        for(BreedView breedView: externalBreedView) {
            String id = breedView.id();
            Optional<Breed> optionalEntity = breedRepository.findByOuterBreedId(id);

            Breed breedUpdateOrCreate = optionalEntity
                    .map(existingEntity -> {
                        breedRepository.deleteById(optionalEntity.get().getId());
                        Breed newUpdateBreed = new Breed();
                        newUpdateBreed.setOuterBreedId(id);
                        newUpdateBreed.setBreedName(breedView.name());
                        newUpdateBreed.setDescription(breedView.description());
                        return newUpdateBreed;
                    })
                    .orElseGet(() -> {
                        Breed newSaveBreed = new Breed();
                        newSaveBreed.setOuterBreedId(id);
                        newSaveBreed.setBreedName(breedView.name());
                        newSaveBreed.setDescription(breedView.description());
                        return newSaveBreed;
                    });
            breedRepository.save(breedUpdateOrCreate);
        }
        return "Breeds are updated";
    }
}