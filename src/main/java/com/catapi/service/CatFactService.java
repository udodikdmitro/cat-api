package com.catapi.service;

import com.catapi.entity.CatFact;
import com.catapi.exception.ExternalApiException;
import com.catapi.jpa.CatFactRepository;
import com.catapi.view.CatFactResponse;
import com.catapi.view.CatFactView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class CatFactService{

    public static final String CAT_API_URL = "https://catfact.ninja/facts";
    private final RestTemplate restTemplate;
    private final CatFactRepository catFactRepository;

    @Autowired
    public CatFactService(RestTemplate restTemplate, CatFactRepository catFactRepository) {
        this.restTemplate = restTemplate;
        this.catFactRepository = catFactRepository;
    }

    public List<CatFactView> getCatFactsFromApi() {
        CatFactResponse catFactResponse = requestCatFacts(CAT_API_URL);

        if (catFactResponse == null) {
            return Collections.emptyList();
        }

        int lastPage = catFactResponse.getLastPage();
        List<CatFactView> catFactViewList = new ArrayList<>();

        for (int i = 1; i <= lastPage; i++) {
            String currentPageUrl = CAT_API_URL + "?page=" + i;
            CatFactResponse response = requestCatFacts(currentPageUrl);

            if (response != null) {
                List<CatFactView> currentListCatFactView = response.getData();
                if (currentListCatFactView != null) {
                    catFactViewList.addAll(currentListCatFactView);
                }
            }
        }
        return catFactViewList;
    }

    private CatFactResponse requestCatFacts(String catApiUrl) {
        try {
            return restTemplate.getForObject(catApiUrl, CatFactResponse.class);
        } catch (Exception e){
            throw new ExternalApiException("Cannot get cat fact from external api: " + e.getMessage());
        }
    }

    public void saveNewFactsFromExternalApi(){
        List<CatFactView> externalApiFacts = getCatFactsFromApi();
        Set<String> dbFacts = catFactRepository.getAllFacts();

        for(CatFactView externalApiFact: externalApiFacts){
            boolean isFactNew = dbFacts.add(externalApiFact.getFact());

            if (isFactNew){
                CatFact catFact = new CatFact();
                catFact.setFact(externalApiFact.getFact());
                catFactRepository.save(catFact);
            }
        }
    }

    public long getNumberOfFacts() {
        return catFactRepository.count();
    }
}