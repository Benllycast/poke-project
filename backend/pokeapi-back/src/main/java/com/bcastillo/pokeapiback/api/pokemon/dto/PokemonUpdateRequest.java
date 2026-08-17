package com.bcastillo.pokeapiback.api.pokemon.dto;

import com.bcastillo.pokeapiback.domain.model.EvolutionStage;
import com.bcastillo.pokeapiback.domain.model.StatValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PokemonUpdateRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String spriteUrl,
        @Size(max = 100) String category,
        @PositiveOrZero Integer weight,
        @PositiveOrZero Integer height,
        List<String> abilities,
        List<String> moves,
        List<StatValue> stats,
        List<String> types,
        String description,
        List<EvolutionStage> evolutionChain,
        @Size(max = 100) String localizedName,
        @Size(max = 100) String region,
        @Size(max = 500) String tags
) {
}
