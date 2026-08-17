package com.bcastillo.pokeapiback.application.pokemon;

import com.bcastillo.pokeapiback.domain.exception.PokemonNotFoundException;
import com.bcastillo.pokeapiback.domain.model.EvolutionStage;
import com.bcastillo.pokeapiback.domain.model.PageResult;
import com.bcastillo.pokeapiback.domain.model.Pokemon;
import com.bcastillo.pokeapiback.domain.model.StatValue;
import com.bcastillo.pokeapiback.domain.port.PokemonRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PokemonQueryServiceTest {

    @Mock
    private PokemonRepositoryPort pokemonRepositoryPort;

    private PokemonQueryService service;

    @BeforeEach
    void setUp() {
        service = new PokemonQueryService(pokemonRepositoryPort);
    }

    @Test
    void getById_found_returnsPokemon() {
        Pokemon pokemon = pokemon(35L);
        when(pokemonRepositoryPort.findById(35L)).thenReturn(Optional.of(pokemon));

        assertThat(service.getById(35L)).isEqualTo(pokemon);
    }

    @Test
    void getById_notFound_throwsPokemonNotFoundException() {
        when(pokemonRepositoryPort.findById(9999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(9999L)).isInstanceOf(PokemonNotFoundException.class);
    }

    @Test
    void list_clampsNegativePageToZero() {
        when(pokemonRepositoryPort.findAll(0, 20)).thenReturn(emptyPage());

        service.list(-5, 20);

        verify(pokemonRepositoryPort).findAll(0, 20);
    }

    @Test
    void list_clampsOversizedSizeTo100() {
        when(pokemonRepositoryPort.findAll(0, 100)).thenReturn(emptyPage());

        service.list(0, 99999);

        verify(pokemonRepositoryPort).findAll(0, 100);
    }

    private PageResult<Pokemon> emptyPage() {
        return new PageResult<>(List.of(), 0, 0, 0, 0);
    }

    private Pokemon pokemon(long id) {
        return new Pokemon(id, "clefairy", "sprite.png", "Fairy Pokémon", 75, 6,
                List.of("friend-guard"), List.of("pound"), List.of(new StatValue("speed", 35, 0)),
                List.of("fairy"), "desc", List.of(new EvolutionStage(173, "cleffa", null)),
                null, null, null);
    }
}
