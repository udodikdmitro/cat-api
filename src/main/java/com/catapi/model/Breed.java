package com.catapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOuterBreedId() {
        return outerBreedId;
    }

    public void setOuterBreedId(String outerBreedId) {
        this.outerBreedId = outerBreedId;
    }

    public String getBreedName() {
        return breedName;
    }

    public void setBreedName(String breedName) {
        this.breedName = breedName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}