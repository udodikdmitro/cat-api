package com.catapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(name = "outer_name")
    private String breedName;

    @Column(name = "description")
    private String description;
}