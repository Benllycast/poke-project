package com.bcastillo.pokeapiback.domain.port;

import com.bcastillo.pokeapiback.domain.model.PageResult;
import com.bcastillo.pokeapiback.domain.model.Pokemon;

import java.util.Optional;

public interface PokemonRepositoryPort {

    Optional<Pokemon> findById(Long id);

    Pokemon save(Pokemon pokemon);

    PageResult<Pokemon> findAll(int page, int size);

    void deleteById(Long id);
}
