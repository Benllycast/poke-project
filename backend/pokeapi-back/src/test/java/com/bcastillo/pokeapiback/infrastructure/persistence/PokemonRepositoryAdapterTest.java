package com.bcastillo.pokeapiback.infrastructure.persistence;

import com.bcastillo.pokeapiback.domain.model.EvolutionStage;
import com.bcastillo.pokeapiback.domain.model.PageResult;
import com.bcastillo.pokeapiback.domain.model.Pokemon;
import com.bcastillo.pokeapiback.domain.model.StatValue;
import com.bcastillo.pokeapiback.infrastructure.persistence.mapper.PokemonEntityMapper;
import com.bcastillo.pokeapiback.infrastructure.persistence.repository.PokemonJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PokemonRepositoryAdapterTest {

    @Autowired
    private PokemonJpaRepository jpaRepository;

    private PokemonRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PokemonRepositoryAdapter(jpaRepository, new PokemonEntityMapper());
    }

    @Test
    void save_thenFindById_roundTripsTypedFields() {
        Pokemon pokemon = minimalPokemon(35L, "clefairy");

        adapter.save(pokemon);
        Optional<Pokemon> reloaded = adapter.findById(35L);

        assertThat(reloaded).contains(pokemon);
    }

    @Test
    void findById_returnsEmptyWhenMissing() {
        assertThat(adapter.findById(9999L)).isEmpty();
    }

    @Test
    void findAll_paginatesAcrossSavedRecords() {
        adapter.save(minimalPokemon(1L, "bulbasaur"));
        adapter.save(minimalPokemon(2L, "ivysaur"));
        adapter.save(minimalPokemon(3L, "venusaur"));

        PageResult<Pokemon> firstPage = adapter.findAll(0, 2);

        assertThat(firstPage.items()).hasSize(2);
        assertThat(firstPage.page()).isEqualTo(0);
        assertThat(firstPage.size()).isEqualTo(2);
        assertThat(firstPage.totalElements()).isEqualTo(3);
        assertThat(firstPage.totalPages()).isEqualTo(2);
    }

    @Test
    void deleteById_removesTheRecord() {
        adapter.save(minimalPokemon(35L, "clefairy"));

        adapter.deleteById(35L);

        assertThat(adapter.findById(35L)).isEmpty();
    }

    private Pokemon minimalPokemon(long id, String name) {
        return new Pokemon(
                id, name, "https://example.com/" + id + ".png", "Fairy Pokémon", 75, 6,
                List.of("friend-guard"), List.of("pound"),
                List.of(new StatValue("speed", 35, 0)), List.of("fairy"),
                "desc",
                List.of(new EvolutionStage(173, "cleffa", null)),
                "Fée-Fée", "Kanto", "starter,cute"
        );
    }
}
