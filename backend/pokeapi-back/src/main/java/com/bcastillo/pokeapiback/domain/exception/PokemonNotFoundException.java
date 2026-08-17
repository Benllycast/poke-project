package com.bcastillo.pokeapiback.domain.exception;

public class PokemonNotFoundException extends RuntimeException {

    public PokemonNotFoundException(Long id) {
        super("Pokemon not found: " + id);
    }
}
