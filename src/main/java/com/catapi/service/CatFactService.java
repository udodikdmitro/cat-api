package com.catapi.service;

import com.catapi.entity.CatFact;
import com.catapi.exception.ExternalApiException;
import com.catapi.jpa.CatFactRepository;
import com.catapi.view.CatFactDataResponse;
import com.catapi.view.CatFactLastPageResponse;
import com.catapi.view.CatFactView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Slf4j
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
        CatFactLastPageResponse catFactLastPageResponse = requestLastPage();

        if (catFactLastPageResponse == null) {
            return Collections.emptyList();
        }

        int lastPage = catFactLastPageResponse.getLastPage();
        List<CatFactView> catFactViewList = new ArrayList<>();

        for (int i = 1; i <= lastPage; i++) {
            String currentPageUrl = CAT_API_URL + "?page=" + i;
            CatFactDataResponse response = requestCatFacts(currentPageUrl);

            if (response != null) {
                List<CatFactView> currentListCatFactView = response.getData();
                if (currentListCatFactView != null) {
                    catFactViewList.addAll(currentListCatFactView);
                }
            }
        }
        return catFactViewList;
    }

    private CatFactDataResponse requestCatFacts(String catApiUrl) {
        try {
            return restTemplate.getForObject(catApiUrl, CatFactDataResponse.class);
        } catch (Exception e){
            throw new ExternalApiException("Cannot get cat fact from external api: " + e.getMessage());
        }
    }

    private CatFactLastPageResponse requestLastPage() {
        try {
            return restTemplate.getForObject(CAT_API_URL, CatFactLastPageResponse.class);
        } catch (Exception e){
            throw new ExternalApiException("Cannot get number of pages from external api: " + e.getMessage());
        }
    }

    public void saveNewFactsFromExternalApi(){
        List<CatFactView> externalApiFacts = getCatFactsFromApi();
        Set<String> dbFacts = catFactRepository.getAllFacts();

        for(CatFactView externalApiFact: externalApiFacts){
            String factWithoutNBSP = externalApiFact.fact().replace("\u00A0", " ");
            boolean isFactNew = dbFacts.add(factWithoutNBSP);

            if (isFactNew){
                log.debug("New cat fact is appeared: {}", factWithoutNBSP);
                CatFact catFact = new CatFact();
                catFact.setFact(factWithoutNBSP);
                catFactRepository.save(catFact);
            }
        }
        log.info("Cat facts are updated");
    }

    public long getNumberOfFacts() {
        return catFactRepository.count();
    }
}