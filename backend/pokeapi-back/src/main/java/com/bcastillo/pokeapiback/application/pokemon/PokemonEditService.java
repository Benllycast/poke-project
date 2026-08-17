package com.bcastillo.pokeapiback.application.pokemon;

import com.bcastillo.pokeapiback.domain.exception.PokemonAlreadyExistsException;
import com.bcastillo.pokeapiback.domain.exception.PokemonNotFoundException;
import com.bcastillo.pokeapiback.domain.model.Pokemon;
import com.bcastillo.pokeapiback.domain.port.PokemonRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class PokemonEditService {

    private final PokemonRepositoryPort pokemonRepositoryPort;

    public PokemonEditService(PokemonRepositoryPort pokemonRepositoryPort) {
        this.pokemonRepositoryPort = pokemonRepositoryPort;
    }

    public Pokemon create(Pokemon pokemon) {
        if (pokemonRepositoryPort.findById(pokemon.id()).isPresent()) {
            throw new PokemonAlreadyExistsException(pokemon.id());
        }
        return pokemonRepositoryPort.save(pokemon);
    }

    public Pokemon update(Long id, Pokemon pokemon) {
        pokemonRepositoryPort.findById(id).orElseThrow(() -> new PokemonNotFoundException(id));
        return pokemonRepositoryPort.save(pokemon);
    }

    public void delete(Long id) {
        pokemonRepositoryPort.findById(id).orElseThrow(() -> new PokemonNotFoundException(id));
        pokemonRepositoryPort.deleteById(id);
    }
}
