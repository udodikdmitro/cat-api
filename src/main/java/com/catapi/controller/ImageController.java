package com.catapi.controller;

import com.catapi.service.CatImageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/image")
public class ImageController {
    private final CatImageService catImageService;

    public ImageController(CatImageService catImageService) {
        this.catImageService = catImageService;
    }

    @GetMapping("/update")
    public void updateBreedsFromExternalApiController() {
        catImageService.getAndSaveAllCatImagesFromExternalApi();
    }
}