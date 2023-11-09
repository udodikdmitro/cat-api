package com.catapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cat_fact")
public class CatFact {

    @Id
    @Column(name = "id")
    private long id;

    @Column(name = "fact")
    private String fact;

    public CatFact() {}

    public CatFact(long id, String fact) {
        this.id = id;
        this.fact = fact;
    }
}