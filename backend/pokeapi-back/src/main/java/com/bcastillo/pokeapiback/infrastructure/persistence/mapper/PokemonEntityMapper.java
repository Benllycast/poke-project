package com.bcastillo.pokeapiback.infrastructure.persistence.mapper;

import com.bcastillo.pokeapiback.domain.model.EvolutionStage;
import com.bcastillo.pokeapiback.domain.model.Pokemon;
import com.bcastillo.pokeapiback.domain.model.StatValue;
import com.bcastillo.pokeapiback.infrastructure.persistence.entity.PokemonEntity;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
public class PokemonEntityMapper {

    private final ObjectMapper json = new ObjectMapper();

    public PokemonEntity toEntity(Pokemon pokemon) {
        return new PokemonEntity(
                pokemon.id(),
                pokemon.name(),
                pokemon.spriteUrl(),
                pokemon.category(),
                pokemon.weight(),
                pokemon.height(),
                json.writeValueAsString(pokemon.abilities()),
                json.writeValueAsString(pokemon.moves()),
                json.writeValueAsString(pokemon.stats()),
                json.writeValueAsString(pokemon.types()),
                pokemon.description(),
                json.writeValueAsString(pokemon.evolutionChain()),
                pokemon.localizedName(),
                pokemon.region(),
                pokemon.tags()
        );
    }

    public Pokemon toDomain(PokemonEntity entity) {
        return new Pokemon(
                entity.getId(),
                entity.getName(),
                entity.getSpriteUrl(),
                entity.getCategory(),
                entity.getWeight(),
                entity.getHeight(),
                readList(entity.getAbilitiesJson(), String.class),
                readList(entity.getMovesJson(), String.class),
                readList(entity.getStatsJson(), StatValue.class),
                readList(entity.getTypesJson(), String.class),
                entity.getDescription(),
                readList(entity.getEvolutionChainJson(), EvolutionStage.class),
                entity.getLocalizedName(),
                entity.getRegion(),
                entity.getTags()
        );
    }

    private <T> List<T> readList(String jsonText, Class<T> elementType) {
        if (jsonText == null) {
            return List.of();
        }
        return json.readValue(jsonText, json.getTypeFactory().constructCollectionType(List.class, elementType));
    }
}
