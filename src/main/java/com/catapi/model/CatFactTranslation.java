package com.catapi.model;

import com.catapi.enums.Locale;
import jakarta.persistence.*;

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