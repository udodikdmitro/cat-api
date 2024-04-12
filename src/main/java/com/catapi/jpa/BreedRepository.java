package com.catapi.jpa;

import com.catapi.entity.Breed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.Set;

public interface BreedRepository extends JpaRepository<Breed, Long> {
    Optional<Breed> findByOuterBreedId(String outerBreedId);
    @Query("SELECT breed.outerBreedId FROM Breed as breed")
    Set<String> getAllOuterBreedId();
}