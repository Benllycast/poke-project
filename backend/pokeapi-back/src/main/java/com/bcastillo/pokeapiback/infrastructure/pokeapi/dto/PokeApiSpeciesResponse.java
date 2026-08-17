package com.bcastillo.pokeapiback.infrastructure.pokeapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PokeApiSpeciesResponse {

    public List<Genus> genera;
    public List<FlavorText> flavor_text_entries;
    public NamedApiResource evolution_chain;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Genus {
        public String genus;
        public NamedApiResource language;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FlavorText {
        public String flavor_text;
        public NamedApiResource language;
    }
}
