package com.catapi.view;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CatFactResponse {

    @JsonProperty("data")
    private List<CatFactView> data;

    @JsonProperty("last_page")
    private int lastPage;
}