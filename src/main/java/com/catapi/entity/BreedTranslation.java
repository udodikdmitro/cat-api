package com.catapi.entity;

import com.catapi.enums.Locale;
import com.catapi.enums.UpdateMode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "breed_translation")
@NoArgsConstructor
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

    @Column(name = "update_mode")
    @Enumerated(EnumType.STRING)
    private UpdateMode updateMode;
}