package com.catapi.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Objects;

@Getter
public record CatBreedView(
        String id,
        String name,
        String origin,
        String description,

        @JsonProperty("wikipedia_url")
        String wikipediaUrl
) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CatBreedView that = (CatBreedView) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(origin, that.origin)
                && Objects.equals(description, that.description) && Objects.equals(wikipediaUrl, that.wikipediaUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, origin, description, wikipediaUrl);
    }
}
