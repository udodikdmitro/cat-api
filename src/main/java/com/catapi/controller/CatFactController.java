package com.catapi.controller;

import com.catapi.model.CatFact;
import com.catapi.service.CatFactService;
import com.catapi.view.CatFactResponse;
import com.catapi.view.CatFactView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/")
public class CatFactController {

    private final CatFactService catFactService;

    @Autowired
    public CatFactController(CatFactService catFactService){
        this.catFactService = catFactService;
    }

    @GetMapping("/test")
    public List<CatFactView> test(){
        return catFactService.test();
    }
}