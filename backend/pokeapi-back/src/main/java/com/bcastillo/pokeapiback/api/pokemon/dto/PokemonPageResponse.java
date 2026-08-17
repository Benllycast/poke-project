package com.bcastillo.pokeapiback.api.pokemon.dto;

import java.util.List;

public record PokemonPageResponse(
        List<PokemonResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
