package com.bcastillo.pokeapiback.infrastructure.pokeapi.mapper;

import com.bcastillo.pokeapiback.domain.model.EvolutionStage;
import com.bcastillo.pokeapiback.domain.model.Pokemon;
import com.bcastillo.pokeapiback.domain.model.StatValue;
import com.bcastillo.pokeapiback.infrastructure.pokeapi.dto.NamedApiResource;
import com.bcastillo.pokeapiback.infrastructure.pokeapi.dto.PokeApiEvolutionChainResponse;
import com.bcastillo.pokeapiback.infrastructure.pokeapi.dto.PokeApiPokemonResponse;
import com.bcastillo.pokeapiback.infrastructure.pokeapi.dto.PokeApiSpeciesResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PokeApiResponseMapper {

    private static final int MAX_MOVES = 20;

    public Pokemon toDomain(PokeApiPokemonResponse pokemon, PokeApiSpeciesResponse species,
                             PokeApiEvolutionChainResponse evolutionChain) {
        return new Pokemon(
                pokemon.id,
                pokemon.name,
                pokemon.sprites != null ? pokemon.sprites.front_default : null,
                englishGenus(species),
                pokemon.weight,
                pokemon.height,
                abilities(pokemon),
                moves(pokemon),
                stats(pokemon),
                types(pokemon),
                englishDescription(species),
                evolutionStages(evolutionChain),
                null,
                null,
                null
        );
    }

    private List<String> abilities(PokeApiPokemonResponse pokemon) {
        List<String> result = new ArrayList<>();
        for (PokeApiPokemonResponse.AbilitySlot slot : pokemon.abilities) {
            result.add(slot.ability.name);
        }
        return result;
    }

    private List<String> moves(PokeApiPokemonResponse pokemon) {
        List<String> result = new ArrayList<>();
        for (PokeApiPokemonResponse.MoveSlot slot : pokemon.moves) {
            if (result.size() >= MAX_MOVES) {
                break;
            }
            result.add(slot.move.name);
        }
        return result;
    }

    private List<StatValue> stats(PokeApiPokemonResponse pokemon) {
        List<StatValue> result = new ArrayList<>();
        for (PokeApiPokemonResponse.StatSlot slot : pokemon.stats) {
            result.add(new StatValue(slot.stat.name, slot.base_stat, slot.effort));
        }
        return result;
    }

    private List<String> types(PokeApiPokemonResponse pokemon) {
        List<String> result = new ArrayList<>();
        for (PokeApiPokemonResponse.TypeSlot slot : pokemon.types) {
            result.add(slot.type.name);
        }
        return result;
    }

    private String englishGenus(PokeApiSpeciesResponse species) {
        for (PokeApiSpeciesResponse.Genus genus : species.genera) {
            if ("en".equals(genus.language.name)) {
                return genus.genus;
            }
        }
        return null;
    }

    private String englishDescription(PokeApiSpeciesResponse species) {
        for (PokeApiSpeciesResponse.FlavorText entry : species.flavor_text_entries) {
            if ("en".equals(entry.language.name)) {
                return entry.flavor_text.replace('\n', ' ').replace('\f', ' ');
            }
        }
        return "";
    }

    private List<EvolutionStage> evolutionStages(PokeApiEvolutionChainResponse evolutionChain) {
        List<EvolutionStage> stages = new ArrayList<>();
        PokeApiEvolutionChainResponse.ChainLink current = evolutionChain.chain;
        Integer minLevel = null;
        while (current != null) {
            stages.add(new EvolutionStage(speciesId(current.species), current.species.name, minLevel));
            // Branching evolutions (e.g. Eevee) exist; this only follows the first branch.
            if (current.evolves_to == null || current.evolves_to.isEmpty()) {
                current = null;
            } else {
                PokeApiEvolutionChainResponse.ChainLink next = current.evolves_to.get(0);
                minLevel = (next.evolution_details != null && !next.evolution_details.isEmpty())
                        ? next.evolution_details.get(0).min_level
                        : null;
                current = next;
            }
        }
        return stages;
    }

    private long speciesId(NamedApiResource resource) {
        String trimmed = resource.url.endsWith("/")
                ? resource.url.substring(0, resource.url.length() - 1)
                : resource.url;
        return Long.parseLong(trimmed.substring(trimmed.lastIndexOf('/') + 1));
    }
}
