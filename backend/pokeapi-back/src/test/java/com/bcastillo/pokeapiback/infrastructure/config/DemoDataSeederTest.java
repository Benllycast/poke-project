package com.bcastillo.pokeapiback.infrastructure.config;

import com.bcastillo.pokeapiback.application.auth.AuthService;
import com.bcastillo.pokeapiback.application.pokemon.PokemonSyncService;
import com.bcastillo.pokeapiback.domain.exception.UserAlreadyExistsException;
import com.bcastillo.pokeapiback.domain.model.PageResult;
import com.bcastillo.pokeapiback.domain.port.PokemonRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DemoDataSeederTest {

    @Mock
    private AuthService authService;
    @Mock
    private PokemonSyncService pokemonSyncService;
    @Mock
    private PokemonRepositoryPort pokemonRepositoryPort;

    @Test
    void run_seedsUserAndPokemon_whenEnabledAndEmpty() {
        when(pokemonRepositoryPort.findAll(0, 1)).thenReturn(new PageResult<>(List.of(), 0, 1, 0, 0));
        DemoDataSeeder seeder = new DemoDataSeeder(authService, pokemonSyncService, pokemonRepositoryPort,
                true, 20, "demo@pokeapp.dev", "Demo1234!");

        seeder.run();

        verify(authService).register("demo@pokeapp.dev", "Demo1234!");
        verify(pokemonSyncService).syncByIds(eq(IntStream.rangeClosed(1, 20).boxed().toList()));
    }

    @Test
    void run_skipsPokemonSeed_whenTableAlreadyHasData() {
        when(pokemonRepositoryPort.findAll(0, 1)).thenReturn(new PageResult<>(List.of(), 0, 1, 5, 1));
        DemoDataSeeder seeder = new DemoDataSeeder(authService, pokemonSyncService, pokemonRepositoryPort,
                true, 20, "demo@pokeapp.dev", "Demo1234!");

        seeder.run();

        verify(pokemonSyncService, never()).syncByIds(anyList());
    }

    @Test
    void run_swallowsUserAlreadyExists() {
        when(authService.register("demo@pokeapp.dev", "Demo1234!"))
                .thenThrow(new UserAlreadyExistsException("demo@pokeapp.dev"));
        when(pokemonRepositoryPort.findAll(0, 1)).thenReturn(new PageResult<>(List.of(), 0, 1, 5, 1));
        DemoDataSeeder seeder = new DemoDataSeeder(authService, pokemonSyncService, pokemonRepositoryPort,
                true, 20, "demo@pokeapp.dev", "Demo1234!");

        seeder.run();
    }

    @Test
    void run_doesNothing_whenDisabled() {
        DemoDataSeeder seeder = new DemoDataSeeder(authService, pokemonSyncService, pokemonRepositoryPort,
                false, 20, "demo@pokeapp.dev", "Demo1234!");

        seeder.run();

        verifyNoInteractions(authService, pokemonSyncService, pokemonRepositoryPort);
    }
}
