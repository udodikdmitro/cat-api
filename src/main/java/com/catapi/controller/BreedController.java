package com.catapi.controller;

import com.catapi.service.BreedService;
import com.catapi.view.BreedView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/breed")
public class BreedController {
    private final BreedService breedService;

    public BreedController(BreedService breedService) {
        this.breedService = breedService;
    }

    @GetMapping("/test")
    public List<BreedView> updateCatFacts() {
        return breedService.getAllBreedsFromExternalApi();
    }

}