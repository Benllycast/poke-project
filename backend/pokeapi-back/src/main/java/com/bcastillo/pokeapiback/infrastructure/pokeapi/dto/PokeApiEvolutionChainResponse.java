package com.bcastillo.pokeapiback.infrastructure.pokeapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PokeApiEvolutionChainResponse {

    public ChainLink chain;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChainLink {
        public NamedApiResource species;
        public List<EvolutionDetail> evolution_details;
        public List<ChainLink> evolves_to;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EvolutionDetail {
        public Integer min_level;
    }
}
