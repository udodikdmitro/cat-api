package com.catapi.service;

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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CatImageService {
    public static final String CAT_IMAGE_API_URL =
            "https://api.thecatapi.com/v1/images/search?limit=100&page=";
    public static final String BREED_IDS_PART = "&breed_ids=";
    public static final String API_KEY_PART = "&api_key=";

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

    public void saveImageFromURL(String imageUrl, String savePath) {
        try {
            URI uri = new URI("https://www.example.com");
            URL url = uri.toURL();
            InputStream inputStream = url.openStream();
            String fileName = getFileName(imageUrl);
            String fullImagePath = STR."\{savePath}\\\{fileName}";
            Path filePath = Paths.get(fullImagePath);
            Files.createFile(filePath);
            Files.copy(
                    inputStream,
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );
            log.debug("Image file is saved {}", fileName);
        } catch (IOException | URISyntaxException e) {
            log.error("Cannot save the image {}", e.getMessage());
        }
    }

    public void getAndSaveAllCatImagesFromExternalApi() {
        for (String outerBreedId: bredRepository.getAllOuterBreedId()){
            Long pageNumber = 0L;
            List<CatImageView> startBreedImagePage = getPageImages(pageNumber, outerBreedId);
            if (!startBreedImagePage.isEmpty()) {
                CatImageView first = startBreedImagePage.getFirst();
                String firstId = first.id();

                while (true) {
                    pageNumber++;
                    List<CatImageView> currentBreedImagePage = getPageImages(pageNumber, outerBreedId);
                    if (currentBreedImagePage.isEmpty()){
                        break;
                    }
                    CatImageView firstOfPage = startBreedImagePage.getFirst();
                    String firstOfPageId = firstOfPage.id();
                    if (firstId.equals(firstOfPageId)) {
                        break;
                    } else {
                        startBreedImagePage.addAll(currentBreedImagePage);
                        firstId = firstOfPageId;
                    }
                }
                for (CatImageView catImageView : startBreedImagePage) {
                    CatImage catImage = new CatImage();
                    saveImageFromURL(catImageView.url(), rootFolder);
                    bredRepository.findByOuterBreedId(outerBreedId).ifPresent(catImage::setBreed);
                    catImage.setFileLocation(rootFolder);
                    catImage.setExternalId(catImageView.id());
                    catImageRepository.save(catImage);
                }
            }
        }
    }

    private List<CatImageView> getPageImages(Long pageNumber, String outerBreedId) {
        ResponseEntity<List<CatImageView>> response = generalImageRequest(pageNumber, outerBreedId);
        return Optional.ofNullable(response.getBody())
                .orElseThrow(() -> new ExternalApiException("No body in response"));
    }

    private ResponseEntity<List<CatImageView>> generalImageRequest(Long pageNumber, String breadId){
        try{
            return restTemplate.exchange(
                    STR."\{CAT_IMAGE_API_URL}\{pageNumber}\{BREED_IDS_PART}\{breadId}\{API_KEY_PART}\{apiKey}",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>(){}
            );
        } catch (Exception e){
            throw new ExternalApiException("Cannot get cat image response from external api: "
                    + e.getMessage());
        }
    }

    private String getFileName(String imageUrl){
        int lastSlashIndex = imageUrl.lastIndexOf("/");
        if (lastSlashIndex != -1) {
            return imageUrl.substring(lastSlashIndex + 1);
        } else {
            throw new ExternalApiException("Url must contain /");
        }
    }
}