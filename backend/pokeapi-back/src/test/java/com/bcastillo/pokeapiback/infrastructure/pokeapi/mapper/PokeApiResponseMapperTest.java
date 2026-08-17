package com.bcastillo.pokeapiback.infrastructure.pokeapi.mapper;

import com.bcastillo.pokeapiback.domain.model.EvolutionStage;
import com.bcastillo.pokeapiback.domain.model.Pokemon;
import com.bcastillo.pokeapiback.infrastructure.pokeapi.dto.NamedApiResource;
import com.bcastillo.pokeapiback.infrastructure.pokeapi.dto.PokeApiEvolutionChainResponse;
import com.bcastillo.pokeapiback.infrastructure.pokeapi.dto.PokeApiPokemonResponse;
import com.bcastillo.pokeapiback.infrastructure.pokeapi.dto.PokeApiSpeciesResponse;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PokeApiResponseMapperTest {

    private final PokeApiResponseMapper mapper = new PokeApiResponseMapper();
    private final ObjectMapper json = new ObjectMapper();

    @Test
    void toDomain_mapsHappyPathFixtures() throws Exception {
        PokeApiPokemonResponse pokemon = readFixture("pokemon-response.json", PokeApiPokemonResponse.class);
        PokeApiSpeciesResponse species = readFixture("pokemon-species-response.json", PokeApiSpeciesResponse.class);
        PokeApiEvolutionChainResponse evolutionChain =
                readFixture("evolution-chain-response.json", PokeApiEvolutionChainResponse.class);

        Pokemon result = mapper.toDomain(pokemon, species, evolutionChain);

        assertThat(result.id()).isEqualTo(35L);
        assertThat(result.name()).isEqualTo("clefairy");
        assertThat(result.spriteUrl()).contains("35.png");
        assertThat(result.category()).isEqualTo("Fairy Pokémon");
        assertThat(result.weight()).isEqualTo(75);
        assertThat(result.height()).isEqualTo(6);
        assertThat(result.abilities()).containsExactly("friend-guard");
        assertThat(result.moves()).containsExactly("pound");
        assertThat(result.types()).containsExactly("fairy");
        assertThat(result.stats()).hasSize(1);
        assertThat(result.stats().get(0).name()).isEqualTo("speed");
        assertThat(result.stats().get(0).baseStat()).isEqualTo(35);
        assertThat(result.description()).isEqualTo("Its magical and cute appeal has many admirers.");
        assertThat(result.evolutionChain()).containsExactly(
                new EvolutionStage(173, "cleffa", null),
                new EvolutionStage(35, "clefairy", 16),
                new EvolutionStage(36, "clefable", 30)
        );
        assertThat(result.localizedName()).isNull();
        assertThat(result.region()).isNull();
        assertThat(result.tags()).isNull();
    }

    @Test
    void toDomain_englishDescription_returnsEmptyWhenNoEnglishFlavorText() {
        PokeApiSpeciesResponse species = speciesWithNoEnglishFlavorText();

        String description = mapper.toDomain(minimalPokemon(), species, singleStageChain()).description();

        assertThat(description).isEmpty();
    }

    @Test
    void toDomain_evolutionChain_singleStageWhenNoPreEvolution() {
        PokeApiEvolutionChainResponse chain = singleStageChain();

        List<EvolutionStage> stages = mapper.toDomain(minimalPokemon(), minimalSpecies(), chain).evolutionChain();

        assertThat(stages).containsExactly(new EvolutionStage(1, "bulbasaur", null));
    }

    @Test
    void toDomain_category_isNullWhenGenusMissingEnglishTranslation() {
        PokeApiSpeciesResponse species = speciesWithNoEnglishGenus();

        String category = mapper.toDomain(minimalPokemon(), species, singleStageChain()).category();

        assertThat(category).isNull();
    }

    private <T> T readFixture(String fileName, Class<T> type) throws Exception {
        File file = new File("src/test/resources/wiremock/" + fileName);
        return json.readValue(file, type);
    }

    private PokeApiPokemonResponse minimalPokemon() {
        PokeApiPokemonResponse pokemon = new PokeApiPokemonResponse();
        pokemon.id = 1;
        pokemon.name = "bulbasaur";
        pokemon.weight = 69;
        pokemon.height = 7;
        pokemon.sprites = new PokeApiPokemonResponse.Sprites();
        pokemon.abilities = List.of();
        pokemon.moves = List.of();
        pokemon.stats = List.of();
        pokemon.types = List.of();
        return pokemon;
    }

    private PokeApiSpeciesResponse minimalSpecies() {
        PokeApiSpeciesResponse species = new PokeApiSpeciesResponse();
        species.genera = List.of(namedGenus("Seed Pokémon", "en"));
        species.flavor_text_entries = List.of(namedFlavorText("A strange seed was planted on its back.", "en"));
        return species;
    }

    private PokeApiSpeciesResponse speciesWithNoEnglishFlavorText() {
        PokeApiSpeciesResponse species = minimalSpecies();
        species.flavor_text_entries = List.of(namedFlavorText("奇妙な　たねを　せなかに", "ja"));
        return species;
    }

    private PokeApiSpeciesResponse speciesWithNoEnglishGenus() {
        PokeApiSpeciesResponse species = minimalSpecies();
        species.genera = List.of(namedGenus("たねポケモン", "ja-Hrkt"));
        return species;
    }

    private PokeApiSpeciesResponse.Genus namedGenus(String genus, String language) {
        PokeApiSpeciesResponse.Genus g = new PokeApiSpeciesResponse.Genus();
        g.genus = genus;
        g.language = languageResource(language);
        return g;
    }

    private PokeApiSpeciesResponse.FlavorText namedFlavorText(String text, String language) {
        PokeApiSpeciesResponse.FlavorText f = new PokeApiSpeciesResponse.FlavorText();
        f.flavor_text = text;
        f.language = languageResource(language);
        return f;
    }

    private NamedApiResource languageResource(String name) {
        NamedApiResource resource = new NamedApiResource();
        resource.name = name;
        return resource;
    }

    private PokeApiEvolutionChainResponse singleStageChain() {
        PokeApiEvolutionChainResponse chain = new PokeApiEvolutionChainResponse();
        chain.chain = new PokeApiEvolutionChainResponse.ChainLink();
        chain.chain.species = speciesResource("bulbasaur", 1);
        chain.chain.evolves_to = List.of();
        return chain;
    }

    private NamedApiResource speciesResource(String name, long id) {
        NamedApiResource resource = new NamedApiResource();
        resource.name = name;
        resource.url = "https://pokeapi.co/api/v2/pokemon-species/" + id + "/";
        return resource;
    }
}
