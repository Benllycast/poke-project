package com.bcastillo.pokeapiback.infrastructure.pokeapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PokeApiPokemonResponse {

    public long id;
    public String name;
    public int weight;
    public int height;
    public Sprites sprites;
    public List<AbilitySlot> abilities;
    public List<MoveSlot> moves;
    public List<StatSlot> stats;
    public List<TypeSlot> types;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sprites {
        public String front_default;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AbilitySlot {
        public NamedApiResource ability;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MoveSlot {
        public NamedApiResource move;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StatSlot {
        public NamedApiResource stat;
        public int base_stat;
        public int effort;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TypeSlot {
        public NamedApiResource type;
    }
}
