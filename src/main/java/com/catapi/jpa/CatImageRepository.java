package com.catapi.jpa;

import com.catapi.entity.CatImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatImageRepository extends JpaRepository<CatImage, Long> {
}
