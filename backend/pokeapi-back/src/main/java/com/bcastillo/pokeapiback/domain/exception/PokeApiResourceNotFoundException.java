package com.bcastillo.pokeapiback.domain.exception;

public class PokeApiResourceNotFoundException extends RuntimeException {

    public PokeApiResourceNotFoundException(String idOrName) {
        super("PokeAPI resource not found: " + idOrName);
    }
}
