package com.catapi.model;

import com.catapi.enums.Locale;
import jakarta.persistence.*;

public class BreedTranslation {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "breed_id", referencedColumnName = "id")
    private Breed breed;

    @Column(name ="locale")
    @Enumerated(EnumType.STRING)
    private Locale locale;

    @Column(name = "breed_name")
    private String breedName;

    @Column(name = "description")
    private String description;
}