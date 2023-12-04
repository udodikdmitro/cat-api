package com.catapi.jpa;

import com.catapi.model.CatFact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatFactRepository extends JpaRepository<CatFact, Long> {


}