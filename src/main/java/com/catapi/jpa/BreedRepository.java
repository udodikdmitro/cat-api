package com.catapi.jpa;

import com.catapi.entity.Breed;
import com.catapi.enums.ActiveState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BreedRepository extends JpaRepository<Breed, Long> {
    Optional<Breed> findByOuterBreedId(String outerBreedId);

    List<Breed> findAllByActiveState(ActiveState activeState);
}