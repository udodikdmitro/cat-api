package com.catapi.view;

import jakarta.validation.constraints.NotBlank;

public record BreedUpdateTranslationView(
        @NotBlank
        String newBreedName,
        @NotBlank
        String newDescription
) {
}