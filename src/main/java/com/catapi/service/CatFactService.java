package com.catapi.service;

import com.catapi.entity.CatFact;
import com.catapi.enums.ActiveState;
import com.catapi.exception.ExternalApiException;
import com.catapi.jpa.CatFactRepository;
import com.catapi.view.CatFactDataResponse;
import com.catapi.view.CatFactLastPageResponse;
import com.catapi.view.CatFactUpdateView;
import com.catapi.view.CatFactView;
import jakarta.persistence.EntityNotFoundException;
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
            throw new ExternalApiException(STR."Cannot get cat fact from external api: \{e.getMessage()}");
        }
    }

    private CatFactLastPageResponse requestLastPage() {
        try {
            return restTemplate.getForObject(CAT_API_URL, CatFactLastPageResponse.class);
        } catch (Exception e){
            throw new ExternalApiException(STR."Cannot get number of pages from external api: \{e.getMessage()}");
        }
    }

    public void saveNewFactsFromExternalApi() {
        List<CatFactView> externalApiFacts = getCatFactsFromApi();
        Set<String> textOfFacts = catFactRepository.getAllTextOfFacts();
        for(CatFactView externalApiFact: externalApiFacts){
            String preparedFactText = externalApiFact.fact().replace("\u00A0", " ").trim();
            boolean isFactNew = textOfFacts.add(preparedFactText);

            if (isFactNew){
                log.debug("New cat fact is appeared: {}", preparedFactText);
                CatFact catFact = new CatFact();
                catFact.setFact(preparedFactText);
                catFact.setActiveState(ActiveState.ACTIVE);
                catFactRepository.save(catFact);
            }
        }
        log.info("Cat facts are updated");
    }

    public void updateCatFact(Long id, CatFactUpdateView catFactUpdateView) {
        final CatFact factToUpdate = catFactRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(STR."Cat fact with id \{id} is not found"));
        factToUpdate.setFact(catFactUpdateView.newFactText());
        catFactRepository.save(factToUpdate);
    }

    public void updateCatFactState(Long id, ActiveState newActiveState) {
        final CatFact factToUpdate = catFactRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(STR."Cat fact with id \{id} is not found"));
        factToUpdate.setActiveState(newActiveState);
        catFactRepository.save(factToUpdate);
    }
    
    

    public long getNumberOfFacts() {
        return catFactRepository.count();
    }


}