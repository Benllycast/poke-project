package com.bcastillo.pokeapiback.domain.model;

import java.util.List;

public record Pokemon(
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

    public Pokemon withProprietaryFields(String localizedName, String region, String tags) {
        return new Pokemon(id, name, spriteUrl, category, weight, height, abilities, moves, stats, types,
                description, evolutionChain, localizedName, region, tags);
    }
}
