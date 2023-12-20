package com.catapi.view;

import com.fasterxml.jackson.annotation.JsonProperty;
public record BreedView(
        String id,

        String name,

        String origin,

        String description,

        @JsonProperty("wikipedia_url")
        String wikipediaUrl
) {}