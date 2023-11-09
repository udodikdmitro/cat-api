package com.catapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "breed")
public class Breed {

    @Id
    @Column(name = "id")
    private long id;

    @Column(name = "outer_breed_id")
    private String outerBreedId;

    @Column(name = "outer_name")
    private String breedName;

    @Column(name = "description")
    private String description;

    public Breed() {}

    public Breed(long id, String outerBreedId, String breedName, String description) {
        this.id = id;
        this.outerBreedId = outerBreedId;
        this.breedName = breedName;
        this.description = description;
    }
}