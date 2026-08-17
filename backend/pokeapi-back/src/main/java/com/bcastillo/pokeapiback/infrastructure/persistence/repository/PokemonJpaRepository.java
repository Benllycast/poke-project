package com.bcastillo.pokeapiback.infrastructure.persistence.repository;

import com.bcastillo.pokeapiback.infrastructure.persistence.entity.PokemonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PokemonJpaRepository extends JpaRepository<PokemonEntity, Long> {
}
