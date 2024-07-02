package com.catapi.service;

import com.catapi.entity.Breed;
import com.catapi.entity.CatImage;
import com.catapi.exception.ExternalApiException;
import com.catapi.jpa.BreedRepository;
import com.catapi.jpa.CatImageRepository;
import com.catapi.view.CatImageView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.catapi.service.FileService.saveImage;

@Slf4j
@Service
public class CatImageService {
    public static final String CAT_IMAGE_API_URL = "https://api.thecatapi.com/v1/images/search?limit=100&page=";
    public static final String BREED_IDS_PART = "&breed_ids=";
    public static final String API_KEY_PART = "&api_key=";
    private final String imageFolder = "images";

    @Value("${settings.folder.root}")
    private String rootFolder;

    @Value("${settings.api-key}")
    private String apiKey;
    private final RestTemplate restTemplate;
    private final CatImageRepository catImageRepository;
    private final BreedRepository bredRepository;

    public CatImageService(RestTemplate restTemplate, CatImageRepository catImageRepository, BreedRepository bredRepository) {
        this.restTemplate = restTemplate;
        this.catImageRepository = catImageRepository;
        this.bredRepository = bredRepository;
    }

    public String saveImageFromURL(String imageUrl, String savePath) {
        String fileName = getFileName(imageUrl);
        String fullImagePath =
                STR. "\{ savePath }\{ getOSSlashSymbol() }\{imageFolder}\{ getOSSlashSymbol() }\{ fileName }" ;
        URL url = getUriFromUrl(imageUrl);
        return saveImage(fullImagePath, url, fileName);
    }

    public void getAndSaveAllCatImagesFromExternalApi() {
        for (Breed breed : bredRepository.findAll()) {
            getAndSaveBreedImages(breed);
        }
        getAndSaveBreedImages(null);
    }

    public void getAndSaveBreedImages(Breed breed) {
        String outerBreedId = breed != null
                ? breed.getOuterBreedId()
                : "";
        Long pageNumber = 0L;
        List<CatImageView> startBreedImagePage = getPageImages(pageNumber, outerBreedId);
        if (!startBreedImagePage.isEmpty()) {
            CatImageView firstImage = startBreedImagePage.getFirst();
            String currentFirstImageId = firstImage.id();

            while (true) {
                pageNumber++;
                List<CatImageView> currentBreedImagePage = getPageImages(pageNumber, outerBreedId);
                if (currentBreedImagePage.isEmpty()) {
                    break;
                }
                CatImageView firstOfPage = startBreedImagePage.getFirst();
                String firstOfPageId = firstOfPage.id();
                if (currentFirstImageId.equals(firstOfPageId)) {
                    break;
                } else {
                    startBreedImagePage.addAll(currentBreedImagePage);
                    currentFirstImageId = firstOfPageId;
                }
            }
            Set<String> repositoryExternalId = new HashSet<>();
            catImageRepository.findAll().forEach(catImage -> repositoryExternalId.add(catImage.getExternalId()));
            for (CatImageView catImageView : startBreedImagePage) {
                if (repositoryExternalId.add(catImageView.id())){
                    CatImage catImage = new CatImage();
                    String fileName = saveImageFromURL(catImageView.url(), rootFolder);
                    catImage.setBreed(breed);
                    catImage.setFileLocation(STR."\{imageFolder}\{ getOSSlashSymbol() }\{ fileName }");
                    catImage.setExternalId(catImageView.id());
                    catImageRepository.save(catImage);
                }
            }
        }
    }

    URL getUriFromUrl(String url) {
        try {
            return new URI(url).toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new ExternalApiException(e.getMessage());
        }
    }

    String getFileName(String imageUrl) {
        int lastSlashIndex = imageUrl.lastIndexOf("/");
        if (lastSlashIndex != -1) {
            return imageUrl.substring(lastSlashIndex + 1);
        } else {
            throw new ExternalApiException("Url must contain /");
        }
    }

    List<CatImageView> getPageImages(Long pageNumber, String outerBreedId) {
        ResponseEntity<List<CatImageView>> response = generalImageRequest(pageNumber, outerBreedId);
        return Optional.ofNullable(response.getBody())
                .orElseThrow(() -> new ExternalApiException("No body in response"));
    }

    private ResponseEntity<List<CatImageView>> generalImageRequest(Long pageNumber, String breadId) {
        try {
            return restTemplate.exchange(
                    STR. "\{ CAT_IMAGE_API_URL }\{ pageNumber }\{ BREED_IDS_PART }\{ breadId }\{ API_KEY_PART }\{ apiKey }" ,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );
        } catch (Exception e) {
            throw new ExternalApiException(STR. "Cannot get cat image response from external api: \{ e.getMessage() }" );
        }
    }

    private String getOSSlashSymbol() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("windows") ? "\\" : "/";
    }
}