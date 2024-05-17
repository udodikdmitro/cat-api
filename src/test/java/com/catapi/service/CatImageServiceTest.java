package com.catapi.service;

import com.catapi.exception.ExternalApiException;
import com.catapi.jpa.BreedRepository;
import com.catapi.jpa.CatImageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.catapi.service.FileService.saveImage;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CatImageServiceTest {

    RestTemplate restTemplate = mock();
    CatImageRepository catImageRepository = mock();
    BreedRepository bredRepository = mock();
    CatImageService catImageService = new CatImageService(restTemplate, catImageRepository, bredRepository);


    @Test
    void saveImageFromURLTest(){
        String imageUrl = "https://example.com/cat.jpg";
        String savePath = "test-images";
        String fullImagePath = "test-images/folder";
        String fileName = "cat.jpg";
        URL url = catImageService.getUriFromUrl(imageUrl);
        when(saveImage(fullImagePath, url, fileName)).thenReturn(fileName);
        String resFileName = catImageService.saveImageFromURL(imageUrl, savePath);
        assertEquals(fileName, resFileName);
    }

    @Test
    void saveImageFromURLInvalidUrlTest() {
        // Arrange
        String invalidImageUrl = "invalid-url";

        // Act & Assert
        assertThrows(ExternalApiException.class, () -> catImageService.saveImageFromURL(invalidImageUrl, "test-images/"));
    }

    @Test
    void saveImageFromURLIOExceptionTest() {
        // Arrange
        String imageUrl = "https://example.com/cat.jpg";

        // Mock URLConnection to throw IOException
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection urlConnection = mock(HttpURLConnection.class);
            when(url.openConnection()).thenReturn(urlConnection);
            when(urlConnection.getInputStream()).thenThrow(new IOException());

            // Act & Assert
            assertThrows(ExternalApiException.class, () -> catImageService.saveImageFromURL(imageUrl, "test-images/"));
        } catch (IOException e) {
            fail("Malformed URL exception occurred during test setup.");
        }
    }
}