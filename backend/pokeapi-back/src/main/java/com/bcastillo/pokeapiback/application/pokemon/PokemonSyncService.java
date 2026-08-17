package com.bcastillo.pokeapiback.application.pokemon;

import com.bcastillo.pokeapiback.domain.model.Pokemon;
import com.bcastillo.pokeapiback.domain.port.PokeApiClientPort;
import com.bcastillo.pokeapiback.domain.port.PokemonRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class PokemonSyncService {

    private final PokeApiClientPort pokeApiClientPort;
    private final PokemonRepositoryPort pokemonRepositoryPort;

    public PokemonSyncService(PokeApiClientPort pokeApiClientPort, PokemonRepositoryPort pokemonRepositoryPort) {
        this.pokeApiClientPort = pokeApiClientPort;
        this.pokemonRepositoryPort = pokemonRepositoryPort;
    }

    public Pokemon syncPokemon(String idOrName) {
        Pokemon replica = pokeApiClientPort.fetchReplicaData(idOrName);
        Pokemon merged = pokemonRepositoryPort.findById(replica.id())
                .map(existing -> replica.withProprietaryFields(
                        existing.localizedName(), existing.region(), existing.tags()))
                .orElse(replica);
        return pokemonRepositoryPort.save(merged);
    }
}
