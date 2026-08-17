package com.bcastillo.pokeapiback.api.pokemon.mapper;

import com.bcastillo.pokeapiback.api.pokemon.dto.PokemonCreateRequest;
import com.bcastillo.pokeapiback.api.pokemon.dto.PokemonPageResponse;
import com.bcastillo.pokeapiback.api.pokemon.dto.PokemonResponse;
import com.bcastillo.pokeapiback.api.pokemon.dto.PokemonUpdateRequest;
import com.bcastillo.pokeapiback.domain.model.PageResult;
import com.bcastillo.pokeapiback.domain.model.Pokemon;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

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

    public Pokemon fromCreateRequest(PokemonCreateRequest request) {
        return new Pokemon(
                request.id(), request.name(), request.spriteUrl(), request.category(), request.weight(),
                request.height(), orEmpty(request.abilities()), orEmpty(request.moves()),
                orEmpty(request.stats()), orEmpty(request.types()), request.description(),
                orEmpty(request.evolutionChain()), request.localizedName(), request.region(), request.tags()
        );
    }

    public Pokemon applyUpdate(Long id, PokemonUpdateRequest request) {
        return new Pokemon(
                id, request.name(), request.spriteUrl(), request.category(), request.weight(),
                request.height(), orEmpty(request.abilities()), orEmpty(request.moves()),
                orEmpty(request.stats()), orEmpty(request.types()), request.description(),
                orEmpty(request.evolutionChain()), request.localizedName(), request.region(), request.tags()
        );
    }

    private <T> List<T> orEmpty(List<T> values) {
        return Objects.requireNonNullElse(values, List.of());
    }
}
