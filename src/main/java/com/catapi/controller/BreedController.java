package com.catapi.controller;

import com.catapi.service.BreedService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/breed")
public class BreedController {
    private final BreedService breedService;

    public BreedController(BreedService breedService) {
        this.breedService = breedService;
    }

    @GetMapping("/update")
    public ResponseEntity<String> updateBreedDbController() {
        breedService.updateBreedDb();
        return ResponseEntity.ok("Breeds are updated");
    }
}