package com.catapi.jpa;

import com.catapi.entity.Breed;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BreedRepository extends JpaRepository<Breed, Long> {
    Optional<Breed> findByOuterBreedId(String outerBreedId);

    @NotNull
    List<Breed> findAll();
}