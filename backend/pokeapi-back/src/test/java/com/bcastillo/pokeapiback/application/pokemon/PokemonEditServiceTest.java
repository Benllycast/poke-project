package com.bcastillo.pokeapiback.application.pokemon;

import com.bcastillo.pokeapiback.domain.exception.PokemonAlreadyExistsException;
import com.bcastillo.pokeapiback.domain.exception.PokemonNotFoundException;
import com.bcastillo.pokeapiback.domain.model.EvolutionStage;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PokemonEditServiceTest {

    @Mock
    private PokemonRepositoryPort pokemonRepositoryPort;

    private PokemonEditService service;

    @BeforeEach
    void setUp() {
        service = new PokemonEditService(pokemonRepositoryPort);
    }

    @Test
    void create_newId_saves() {
        Pokemon pokemon = pokemon(100000L);
        when(pokemonRepositoryPort.findById(100000L)).thenReturn(Optional.empty());
        when(pokemonRepositoryPort.save(pokemon)).thenReturn(pokemon);

        Pokemon result = service.create(pokemon);

        assertThat(result).isEqualTo(pokemon);
    }

    @Test
    void create_existingId_throwsPokemonAlreadyExistsException() {
        Pokemon pokemon = pokemon(35L);
        when(pokemonRepositoryPort.findById(35L)).thenReturn(Optional.of(pokemon));

        assertThatThrownBy(() -> service.create(pokemon)).isInstanceOf(PokemonAlreadyExistsException.class);
        verify(pokemonRepositoryPort, never()).save(any());
    }

    @Test
    void update_existingId_saves() {
        Pokemon existing = pokemon(35L);
        Pokemon updated = pokemon(35L);
        when(pokemonRepositoryPort.findById(35L)).thenReturn(Optional.of(existing));
        when(pokemonRepositoryPort.save(updated)).thenReturn(updated);

        assertThat(service.update(35L, updated)).isEqualTo(updated);
    }

    @Test
    void update_missingId_throwsPokemonNotFoundException() {
        when(pokemonRepositoryPort.findById(9999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(9999L, pokemon(9999L)))
                .isInstanceOf(PokemonNotFoundException.class);
        verify(pokemonRepositoryPort, never()).save(any());
    }

    @Test
    void delete_existingId_deletes() {
        when(pokemonRepositoryPort.findById(35L)).thenReturn(Optional.of(pokemon(35L)));

        service.delete(35L);

        verify(pokemonRepositoryPort).deleteById(35L);
    }

    @Test
    void delete_missingId_throwsPokemonNotFoundException() {
        when(pokemonRepositoryPort.findById(9999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(9999L)).isInstanceOf(PokemonNotFoundException.class);
        verify(pokemonRepositoryPort, never()).deleteById(any());
    }

    private Pokemon pokemon(long id) {
        return new Pokemon(id, "clefairy", "sprite.png", "Fairy Pokémon", 75, 6,
                List.of("friend-guard"), List.of("pound"), List.of(new StatValue("speed", 35, 0)),
                List.of("fairy"), "desc", List.of(new EvolutionStage(173, "cleffa", null)),
                null, null, null);
    }
}
