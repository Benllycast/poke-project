package com.bcastillo.pokeapiback.domain.port;

import com.bcastillo.pokeapiback.domain.model.Pokemon;

public interface PokeApiClientPort {

    /**
     * Fetches and combines a Pokemon's /pokemon, /pokemon-species, and /evolution-chain data from
     * PokeAPI. Proprietary fields (localizedName/region/tags) are always null on the result -
     * merging in existing local edits is the caller's responsibility.
     */
    Pokemon fetchReplicaData(String idOrName);
}
