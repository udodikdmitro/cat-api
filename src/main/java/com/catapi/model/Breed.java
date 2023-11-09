package com.catapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "breed")
public class Breed {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "outer_breed_id")
    private String outerBreedId;

    @Column(name = "outer_name")
    private String breedName;

    @Column(name = "description")
    private String description;

    public Breed() {}

    public Breed(String outerBreedId, String breedName, String description) {
        this.outerBreedId = outerBreedId;
        this.breedName = breedName;
        this.description = description;
    }
}