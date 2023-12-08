package com.catapi.jpa;

import com.catapi.entity.CatFact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Set;

public interface CatFactRepository extends JpaRepository<CatFact, Long> {
    @Query("SELECT fact.fact FROM CatFact as fact")
    Set<String> getAllFacts();

    @Override
    long count();
}