package com.bcastillo.pokeapiback.api.pokemon.mapper;

import com.bcastillo.pokeapiback.api.pokemon.dto.PokemonPageResponse;
import com.bcastillo.pokeapiback.api.pokemon.dto.PokemonResponse;
import com.bcastillo.pokeapiback.domain.model.PageResult;
import com.bcastillo.pokeapiback.domain.model.Pokemon;
import org.springframework.stereotype.Component;

@Component
public class PokemonDtoMapper {

    public PokemonResponse toResponse(Pokemon pokemon) {
        return new PokemonResponse(
                pokemon.id(), pokemon.name(), pokemon.spriteUrl(), pokemon.category(), pokemon.weight(),
                pokemon.height(), pokemon.abilities(), pokemon.moves(), pokemon.stats(), pokemon.types(),
                pokemon.description(), pokemon.evolutionChain(), pokemon.localizedName(), pokemon.region(),
                pokemon.tags()
        );
    }

    public PokemonPageResponse toPageResponse(PageResult<Pokemon> page) {
        return new PokemonPageResponse(
                page.items().stream().map(this::toResponse).toList(),
                page.page(), page.size(), page.totalElements(), page.totalPages()
        );
    }
}
