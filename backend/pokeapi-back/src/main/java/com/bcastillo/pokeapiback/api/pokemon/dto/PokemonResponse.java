package com.bcastillo.pokeapiback.api.pokemon.dto;

import com.bcastillo.pokeapiback.domain.model.EvolutionStage;
import com.bcastillo.pokeapiback.domain.model.StatValue;

import java.util.List;

public record PokemonResponse(
        Long id,
        String name,
        String spriteUrl,
        String category,
        Integer weight,
        Integer height,
        List<String> abilities,
        List<String> moves,
        List<StatValue> stats,
        List<String> types,
        String description,
        List<EvolutionStage> evolutionChain,
        String localizedName,
        String region,
        String tags
) {
}
