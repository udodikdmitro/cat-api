package com.catapi.entity;

import com.catapi.enums.Locale;
import com.catapi.enums.UpdateMode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

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

    @Column(name = "update_mode")
    @Enumerated(EnumType.STRING)
    private UpdateMode updateMode;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CatFactTranslation that = (CatFactTranslation) o;
        return Objects.equals(catFact, that.catFact) && locale == that.locale && Objects.equals(translationText, that.translationText) && updateMode == that.updateMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(catFact, locale, translationText, updateMode);
    }

    @Override
    public String toString() {
        return STR."""
            CatFactTranslation{id=\{id},
            catFact=\{catFact},
            locale=\{locale},
            translationText='\{translationText}\{'\''},
            updateMode=\{updateMode}\{'}'}
        """;
    }
}