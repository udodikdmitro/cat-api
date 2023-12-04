package com.catapi.service;

import com.catapi.view.CatFactResponse;
import com.catapi.view.CatFactView;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class CatFactService {

    public static final String CAT_API_URL = "https://catfact.ninja/facts";
    private final RestTemplate restTemplate;

    public CatFactService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<CatFactView> test() {
        CatFactResponse catFactResponse = restTemplate.getForObject(CAT_API_URL, CatFactResponse.class);

        if (catFactResponse == null) {
            return Collections.emptyList();
        }

        int lastPage = Objects.requireNonNull(catFactResponse).getLastPage();
        List<CatFactView> catFactViewList = new ArrayList<>();

        for (int i = 1; i <= lastPage; i++) {
            CatFactResponse response = restTemplate.getForObject(CAT_API_URL + "?page=" + i, CatFactResponse.class);

            if (response != null) {
                List<CatFactView> currentListCatFactView = response.getData();
                if (currentListCatFactView != null) {
                    catFactViewList.addAll(currentListCatFactView);
                }
            }
        }
        return catFactViewList;
    }
}