package com.catapi.service;

import com.catapi.entity.Breed;
import com.catapi.entity.CatImage;
import com.catapi.exception.ExternalApiException;
import com.catapi.jpa.BreedRepository;
import com.catapi.jpa.CatImageRepository;
import com.catapi.view.CatImageView;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CatImageServiceTest {

    RestTemplate restTemplate = mock();
    CatImageRepository catImageRepository = mock();
    BreedRepository bredRepository = mock();
    URL url = mock();
    HttpURLConnection urlConnection = mock(HttpURLConnection.class);
    CatImageService catImageService = new CatImageService(restTemplate, catImageRepository, bredRepository);


    @Test
    void saveImageFromURL_return_saved_file_name(){
        String imageUrl = "https://example.com/cat.jpg";
        String savePath = "test-images";
        String fullImagePath = "test-images/folder";
        String fileName = "cat.jpg";
        URL url = catImageService.getUriFromUrl(imageUrl);
        MockedStatic<FileService> mockedStatic = mockStatic(FileService.class);
        mockedStatic.when(() -> FileService.saveImage(fullImagePath, url, fileName))
                .thenReturn(fileName);
        FileService.saveImage(fullImagePath, url, fileName);
        String resFileName = catImageService.saveImageFromURL(imageUrl, savePath);
        assertEquals(fileName, resFileName);
    }

    @Test
    void saveImageFromURL_throws_exception_for_invalid_url() {
        String invalidImageUrl = "invalid-url";
        assertThrows(ExternalApiException.class, () -> catImageService.saveImageFromURL(invalidImageUrl, "test-images/"));
    }

    @Test
    void saveImageFromURL_throws_IOException() {
        String imageUrl = "https://example.com/cat.jpg";

        try {
            when(url.openConnection()).thenReturn(urlConnection);
            when(urlConnection.getInputStream()).thenThrow(new IOException());
            assertThrows(ExternalApiException.class, () -> catImageService.saveImageFromURL(imageUrl, "test-images/"));
        } catch (IOException e) {
            fail("Malformed URL exception occurred during test setup.");
        }
    }

    @Test
    void getAndSaveAllCatImagesFromExternalApi_save_all_breeds() {
        // Arrange
        Breed breed1 = new Breed();
        Breed breed2 = new Breed();
        List<CatImageView> catImageViews = new ArrayList<>();
        catImageViews.add(new CatImageView("id 1", "url 1"));
        catImageViews.add(new CatImageView("id 2", "url 2"));
        ResponseEntity<List<CatImageView>> responseEntity = new ResponseEntity<>(catImageViews, HttpStatus.OK);
        ParameterizedTypeReference<List<CatImageView>> responseType = new ParameterizedTypeReference<>() {};
        when(bredRepository.findAll()).thenReturn(Arrays.asList(breed1, breed2));
        when(restTemplate.exchange(any(), any(), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);
        // Act
        catImageService.getAndSaveAllCatImagesFromExternalApi();

        // Assert
        verify(catImageService).getAndSaveBreedImages(breed1);
        verify(catImageService).getAndSaveBreedImages(breed2);
        verify(catImageService).getAndSaveBreedImages(null);
    }

    @Test
    public void testGetAndSaveBreedImages_BreedIsNull() {
        Breed breed = null;
        List<CatImageView> emptyImageList = new ArrayList<>();

        when(catImageService.getPageImages(0L, "")).thenReturn(emptyImageList);

        catImageService.getAndSaveBreedImages(breed);

        verify(catImageRepository, never()).save(any(CatImage.class));
    }

    @Test
    public void testGetAndSaveBreedImages_BreedNotNull_EmptyPages() {
        Breed breed = new Breed();
        breed.setOuterBreedId("1");
        List<CatImageView> emptyImageList = new ArrayList<>();

        when(catImageService.getPageImages(0L, "1")).thenReturn(emptyImageList);

        catImageService.getAndSaveBreedImages(breed);

        verify(catImageRepository, never()).save(any(CatImage.class));
    }

    @Test
    public void testGetAndSaveBreedImages_BreedNotNull_WithImages() {
        Breed breed = new Breed();
        breed.setOuterBreedId("1");

        List<CatImageView> firstPageImages = new ArrayList<>();
        CatImageView firstImage = new CatImageView("1", "http://image1.url");
        firstPageImages.add(firstImage);

        List<CatImageView> secondPageImages = new ArrayList<>();
        CatImageView secondImage = new CatImageView("2", "http://image2.url");
        secondPageImages.add(secondImage);

        when(catImageService.getPageImages(0L, "1")).thenReturn(firstPageImages);
        when(catImageService.getPageImages(1L, "1")).thenReturn(secondPageImages);
        when(catImageService.getPageImages(2L, "1")).thenReturn(new ArrayList<>());
        when(catImageRepository.findAll()).thenReturn(new ArrayList<>());
        when(catImageService.saveImageFromURL(anyString(), anyString())).thenReturn("fileName");

        catImageService.getAndSaveBreedImages(breed);

        verify(catImageRepository, times(2)).save(any(CatImage.class));
    }

    @Test
    public void testGetAndSaveBreedImages_DuplicateImages() {
        Breed breed = new Breed();
        breed.setOuterBreedId("1");

        List<CatImageView> firstPageImages = new ArrayList<>();
        CatImageView firstImage = new CatImageView("1", "http://image1.url");
        firstPageImages.add(firstImage);

        List<CatImageView> secondPageImages = new ArrayList<>();
        CatImageView duplicateImage = new CatImageView("1", "http://image1.url");
        secondPageImages.add(duplicateImage);

        when(catImageService.getPageImages(0L, "1")).thenReturn(firstPageImages);
        when(catImageService.getPageImages(1L, "1")).thenReturn(secondPageImages);
        when(catImageService.getPageImages(2L, "1")).thenReturn(new ArrayList<>());
        when(catImageRepository.findAll()).thenReturn(new ArrayList<>());
        when(catImageService.saveImageFromURL(anyString(), anyString())).thenReturn("fileName");

        catImageService.getAndSaveBreedImages(breed);

        verify(catImageRepository, times(1)).save(any(CatImage.class));
    }
}