package com.catapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "breed")
@NoArgsConstructor
@AllArgsConstructor
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
}