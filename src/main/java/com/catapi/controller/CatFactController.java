package com.catapi.controller;

import com.catapi.service.CatFactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cat-facts")
public class CatFactController {

    private final CatFactService catFactService;

    @Autowired
    public CatFactController(CatFactService catFactService) {
        this.catFactService = catFactService;
    }

    @GetMapping("/update")
    public ResponseEntity<String> updateCatFacts() {
        catFactService.saveNewFactsFromExternalApi();
        return ResponseEntity.ok("Cat facts are updated");
    }

    @GetMapping("/count")
    public String countCatFacts() {
        long result = catFactService.getNumberOfFacts();
        return "The number of facts is " + result;
    }
}