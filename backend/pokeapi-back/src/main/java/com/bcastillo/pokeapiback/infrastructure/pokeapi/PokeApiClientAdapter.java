package com.bcastillo.pokeapiback.infrastructure.pokeapi;

import com.bcastillo.pokeapiback.domain.exception.PokeApiResourceNotFoundException;
import com.bcastillo.pokeapiback.domain.model.Pokemon;
import com.bcastillo.pokeapiback.domain.port.PokeApiClientPort;
import com.bcastillo.pokeapiback.infrastructure.pokeapi.dto.PokeApiEvolutionChainResponse;
import com.bcastillo.pokeapiback.infrastructure.pokeapi.dto.PokeApiPokemonResponse;
import com.bcastillo.pokeapiback.infrastructure.pokeapi.dto.PokeApiSpeciesResponse;
import com.bcastillo.pokeapiback.infrastructure.pokeapi.mapper.PokeApiResponseMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class PokeApiClientAdapter implements PokeApiClientPort {

    private final RestClient restClient;
    private final PokeApiResponseMapper mapper;

    public PokeApiClientAdapter(RestClient pokeApiRestClient, PokeApiResponseMapper mapper) {
        this.restClient = pokeApiRestClient;
        this.mapper = mapper;
    }

    @Override
    public Pokemon fetchReplicaData(String idOrName) {
        PokeApiPokemonResponse pokemon = get("/pokemon/" + idOrName, PokeApiPokemonResponse.class, idOrName);
        PokeApiSpeciesResponse species = get("/pokemon-species/" + idOrName, PokeApiSpeciesResponse.class, idOrName);
        String evolutionChainId = lastPathSegment(species.evolution_chain.url);
        PokeApiEvolutionChainResponse evolutionChain =
                get("/evolution-chain/" + evolutionChainId, PokeApiEvolutionChainResponse.class, idOrName);
        return mapper.toDomain(pokemon, species, evolutionChain);
    }

    private <T> T get(String path, Class<T> responseType, String idOrName) {
        try {
            return restClient.get().uri(path).retrieve().body(responseType);
        } catch (HttpClientErrorException.NotFound e) {
            throw new PokeApiResourceNotFoundException(idOrName);
        }
    }

    private String lastPathSegment(String url) {
        String trimmed = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        return trimmed.substring(trimmed.lastIndexOf('/') + 1);
    }
}
