package com.bcastillo.pokeapiback.infrastructure.persistence;

import com.bcastillo.pokeapiback.domain.model.EvolutionStage;
import com.bcastillo.pokeapiback.domain.model.Pokemon;
import com.bcastillo.pokeapiback.domain.model.StatValue;
import com.bcastillo.pokeapiback.infrastructure.persistence.mapper.PokemonEntityMapper;
import com.bcastillo.pokeapiback.infrastructure.persistence.repository.PokemonJpaRepository;
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

    @Test
    void save_thenFindById_roundTripsTypedFields() {
        PokemonRepositoryAdapter adapter = new PokemonRepositoryAdapter(jpaRepository, new PokemonEntityMapper());
        Pokemon pokemon = new Pokemon(
                35L, "clefairy", "https://example.com/35.png", "Fairy Pokémon", 75, 6,
                List.of("friend-guard"), List.of("pound"),
                List.of(new StatValue("speed", 35, 0)), List.of("fairy"),
                "Its magical and cute appeal has many admirers.",
                List.of(new EvolutionStage(173, "cleffa", null), new EvolutionStage(35, "clefairy", 16)),
                "Fée-Fée", "Kanto", "starter,cute"
        );

        adapter.save(pokemon);
        Optional<Pokemon> reloaded = adapter.findById(35L);

        assertThat(reloaded).contains(pokemon);
    }

    @Test
    void findById_returnsEmptyWhenMissing() {
        PokemonRepositoryAdapter adapter = new PokemonRepositoryAdapter(jpaRepository, new PokemonEntityMapper());

        assertThat(adapter.findById(9999L)).isEmpty();
    }
}
