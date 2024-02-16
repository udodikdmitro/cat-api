package com.catapi.jpa;

import com.catapi.entity.BreedTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BreedTranslationRepository extends JpaRepository<BreedTranslation, Long> {
}
