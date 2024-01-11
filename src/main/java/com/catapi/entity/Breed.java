package com.catapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "breed")
@NoArgsConstructor
public class Breed {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "outer_breed_id")
    private String outerBreedId;

    @Column(name = "breed_name")
    private String breedName;

    @Column(name = "description")
    private String description;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Breed breed = (Breed) o;
        return Objects.equals(id, breed.id) && Objects.equals(outerBreedId, breed.outerBreedId) && Objects.equals(breedName, breed.breedName) && Objects.equals(description, breed.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, outerBreedId, breedName, description);
    }
}