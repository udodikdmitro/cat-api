package com.catapi.entity;

import com.catapi.enums.Locale;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cat_fact_translation")
@NoArgsConstructor
public class CatFactTranslation {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cat_fact_id", referencedColumnName = "id")
    private CatFact catFact;

    @Column(name ="locale")
    @Enumerated(EnumType.STRING)
    private Locale locale;

    @Column(name = "translation_text")
    private String translationText;
}