package com.catapi.service;

import com.catapi.entity.CatImage;
import com.catapi.exception.ExternalApiException;
import com.catapi.jpa.BreedRepository;
import com.catapi.jpa.CatImageRepository;
import com.catapi.view.CatImageView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class CatImageService {
    public static final String CAT_IMAGE_API_URL1 =
            "https://api.thecatapi.com/v1/images/search?limit=100&page=";
    public static final String CAT_IMAGE_API_URL2 = "&breed_ids=";
    public static final String CAT_IMAGE_API_URL3 =
            "&api_key=live_tF1DP6ary6TwsK1YceaeoMQ7B6cRuZFLQr23GFU3ZtVe5AwM8yZSYTkczB4WYXZ2";

    @Value("${settings.folder.root}")
    private String rootFolder;
    private final RestTemplate restTemplate;
    private final CatImageRepository catImageRepository;
    private final BreedRepository bredRepository;

    public CatImageService(RestTemplate restTemplate, CatImageRepository catImageRepository, BreedRepository bredRepository) {
        this.restTemplate = restTemplate;
        this.catImageRepository = catImageRepository;
        this.bredRepository = bredRepository;
    }

    public void saveCatImage(){
        getAndSaveAllCatImagesFromExternalApi();

    }

    public static void saveImageFromURL(String imageUrl, String savePath) {
        try {
            URL url = new URL(imageUrl);
            InputStream inputStream = url.openStream();
            Files.copy(inputStream, Paths.get(savePath), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("The image is saved");
        } catch (IOException e) {
            System.out.println("Can not save image: " + e.getMessage());
        }
    }

    private void getAndSaveAllCatImagesFromExternalApi() {
        for (String outerBreedId: bredRepository.getAllOuterBreedId()){
            List<CatImageView> startCurrentBreedImage = generalImageRequest(0L, outerBreedId).getBody();
            assert startCurrentBreedImage != null;
            String firstId = startCurrentBreedImage.get(0).id();
            Long pageNumber = 0L;

            while (true){
                pageNumber++;
                List<CatImageView> currentBreedImage = generalImageRequest(pageNumber, outerBreedId).getBody();
                assert currentBreedImage != null;
                if (currentBreedImage.isEmpty() || firstId.equals(currentBreedImage.get(0).id())){
                    break;
                } else {
                    startCurrentBreedImage.addAll(currentBreedImage);
                }
            }
            for(CatImageView catImageView : startCurrentBreedImage){
                CatImage catImage = new CatImage();
                saveImageFromURL(catImageView.url(), rootFolder);
                bredRepository.findByOuterBreedId(outerBreedId).ifPresent(catImage::setBreed);
                catImage.setFileLocation(rootFolder);
                catImage.setExternalId(catImageView.id());
                catImageRepository.save(catImage);
            }
        }
    }

    private ResponseEntity<List<CatImageView>> generalImageRequest(Long pageNumber, String breadId){
        try{
            return restTemplate.exchange(CAT_IMAGE_API_URL1 + pageNumber + CAT_IMAGE_API_URL2 +
                            breadId + CAT_IMAGE_API_URL3,
                    HttpMethod.GET, null, new ParameterizedTypeReference<>(){});
        } catch (Exception e){
            throw new ExternalApiException("Cannot get cat image response from external api: "
                    + e.getMessage());
        }
    }
}