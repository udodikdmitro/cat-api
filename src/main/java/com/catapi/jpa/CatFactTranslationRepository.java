package com.catapi.jpa;

import com.catapi.entity.CatFactTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatFactTranslationRepository extends JpaRepository<CatFactTranslation, Long> {
}