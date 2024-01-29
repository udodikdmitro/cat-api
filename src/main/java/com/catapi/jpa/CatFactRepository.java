package com.catapi.jpa;

import com.catapi.entity.CatFact;
import com.catapi.enums.ActiveState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface CatFactRepository extends JpaRepository<CatFact, Long> {
    @Query("SELECT fact.fact FROM CatFact as fact")
    Set<String> getAllTextOfFacts();

    List<CatFact> findAllByActiveState(ActiveState activeState);
}