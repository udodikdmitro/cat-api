package com.catapi.controller;

import com.catapi.enums.ActiveState;
import com.catapi.service.CatFactService;
import com.catapi.view.CatFactUpdateView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cat-facts")
public class CatFactController {

    private final CatFactService catFactService;

    @Autowired
    public CatFactController(CatFactService catFactService) {
        this.catFactService = catFactService;
    }

    @GetMapping("/retrieveExternal")
    public ResponseEntity<String> updateCatFacts() {
        catFactService.saveNewFactsFromExternalApi();
        return ResponseEntity.ok("Cat facts are updated");
    }

    @GetMapping("/count")
    public String countCatFacts() {
        long result = catFactService.getNumberOfFacts();
        return STR."The number of facts is \{result}";
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateCatFact(
            @PathVariable Long id,
            @RequestBody CatFactUpdateView catFactUpdateView
    ) {
        catFactService.updateCatFact(id, catFactUpdateView);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/state/{id}")
    public ResponseEntity<Void> updateCatFactState(
            @PathVariable Long id,
            @RequestParam ActiveState newActiveState
    ) {
        catFactService.updateCatFactState(id, newActiveState);
        return ResponseEntity.ok().build();
    }
}