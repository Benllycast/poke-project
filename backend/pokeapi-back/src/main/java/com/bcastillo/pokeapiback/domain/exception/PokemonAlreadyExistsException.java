package com.bcastillo.pokeapiback.domain.exception;

public class PokemonAlreadyExistsException extends RuntimeException {

    public PokemonAlreadyExistsException(Long id) {
        super("Pokemon already exists: " + id);
    }
}
