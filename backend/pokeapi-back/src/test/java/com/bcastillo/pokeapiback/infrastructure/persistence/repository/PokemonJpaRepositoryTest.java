package com.bcastillo.pokeapiback.infrastructure.persistence.repository;

import com.bcastillo.pokeapiback.infrastructure.persistence.entity.PokemonEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PokemonJpaRepositoryTest {

    @Autowired
    private PokemonJpaRepository repository;

    @Test
    void saveThenReload_everyColumnSurvives() {
        PokemonEntity entity = new PokemonEntity(
                35L, "clefairy", "https://example.com/35.png", "Fairy Pokémon", 75, 6,
                "[\"friend-guard\"]", "[\"pound\"]",
                "[{\"name\":\"speed\",\"baseStat\":35,\"effort\":0}]", "[\"fairy\"]",
                "Its magical and cute appeal has many admirers.",
                "[{\"speciesId\":173,\"name\":\"cleffa\",\"minLevel\":null}]",
                "Fée-Fée", "Kanto", "starter,cute"
        );

        repository.saveAndFlush(entity);

        Optional<PokemonEntity> reloaded = repository.findById(35L);

        assertThat(reloaded).isPresent();
        PokemonEntity found = reloaded.get();
        assertThat(found.getId()).isEqualTo(35L);
        assertThat(found.getName()).isEqualTo("clefairy");
        assertThat(found.getSpriteUrl()).isEqualTo("https://example.com/35.png");
        assertThat(found.getCategory()).isEqualTo("Fairy Pokémon");
        assertThat(found.getWeight()).isEqualTo(75);
        assertThat(found.getHeight()).isEqualTo(6);
        assertThat(found.getAbilitiesJson()).isEqualTo("[\"friend-guard\"]");
        assertThat(found.getMovesJson()).isEqualTo("[\"pound\"]");
        assertThat(found.getStatsJson()).contains("\"speed\"");
        assertThat(found.getTypesJson()).isEqualTo("[\"fairy\"]");
        assertThat(found.getDescription()).isEqualTo("Its magical and cute appeal has many admirers.");
        assertThat(found.getEvolutionChainJson()).contains("cleffa");
        assertThat(found.getLocalizedName()).isEqualTo("Fée-Fée");
        assertThat(found.getRegion()).isEqualTo("Kanto");
        assertThat(found.getTags()).isEqualTo("starter,cute");
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }
}
