package com.catapi.controller;

import com.catapi.service.CatFactService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(CatFactController.class)
class CatFactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CatFactService catFactService;

    @Test
    void update_controller_should_update_expected_response() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/update"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Cat facts are updated"));

        Mockito.verify(catFactService, Mockito.times(1)).saveNewFactsFromExternalApi();
    }
}