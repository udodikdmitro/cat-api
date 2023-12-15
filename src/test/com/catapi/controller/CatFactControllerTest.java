package com.catapi.controller;

import com.catapi.service.CatFactService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.when;

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
    }

    @Test
    void count_controller_should_return_number_of_facts() throws Exception {
        long result = 5L;
        when(catFactService.getNumberOfFacts()).thenReturn(result);
        mockMvc.perform(MockMvcRequestBuilders.get("/count"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("The number of facts is " + result));
    }
}