package com.catapi.entity;

import com.catapi.enums.ActiveState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}