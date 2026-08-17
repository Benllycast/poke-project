package com.bcastillo.pokeapiback.application.pokemon;

import com.bcastillo.pokeapiback.domain.exception.PokemonNotFoundException;
import com.bcastillo.pokeapiback.domain.model.PageResult;
import com.bcastillo.pokeapiback.domain.model.Pokemon;
import com.bcastillo.pokeapiback.domain.port.PokemonRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class PokemonQueryService {

    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 100;

    private final PokemonRepositoryPort pokemonRepositoryPort;

    public PokemonQueryService(PokemonRepositoryPort pokemonRepositoryPort) {
        this.pokemonRepositoryPort = pokemonRepositoryPort;
    }

    public Pokemon getById(Long id) {
        return pokemonRepositoryPort.findById(id)
                .orElseThrow(() -> new PokemonNotFoundException(id));
    }

    public PageResult<Pokemon> list(int page, int size) {
        int clampedPage = Math.max(page, 0);
        int clampedSize = Math.min(Math.max(size, MIN_SIZE), MAX_SIZE);
        return pokemonRepositoryPort.findAll(clampedPage, clampedSize);
    }
}
