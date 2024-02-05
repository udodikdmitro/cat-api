package com.catapi.entity;

import com.catapi.enums.ActiveState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "cat_fact")
@NoArgsConstructor
@AllArgsConstructor
public class CatFact {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fact")
    private String fact;

    @Column(name = "active_state")
    @Enumerated(EnumType.STRING)
    private ActiveState activeState;

    @OneToMany(mappedBy = "catFact", fetch = FetchType.LAZY)
    private List<CatFactTranslation> catFactTranslations;
}