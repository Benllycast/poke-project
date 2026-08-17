package com.bcastillo.pokeapiback.application.pokemon;

import com.bcastillo.pokeapiback.domain.model.EvolutionStage;
import com.bcastillo.pokeapiback.domain.model.Pokemon;
import com.bcastillo.pokeapiback.domain.model.StatValue;
import com.bcastillo.pokeapiback.domain.port.PokeApiClientPort;
import com.bcastillo.pokeapiback.domain.port.PokemonRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PokemonSyncServiceTest {

    @Mock
    private PokeApiClientPort pokeApiClientPort;
    @Mock
    private PokemonRepositoryPort pokemonRepositoryPort;

    private PokemonSyncService service;

    @BeforeEach
    void setUp() {
        service = new PokemonSyncService(pokeApiClientPort, pokemonRepositoryPort);
    }

    @Test
    void syncPokemon_newPokemon_savesWithNullProprietaryFields() {
        Pokemon replica = pokemon(35L, 75, null, null, null);
        when(pokeApiClientPort.fetchReplicaData("35")).thenReturn(replica);
        when(pokemonRepositoryPort.findById(35L)).thenReturn(Optional.empty());
        when(pokemonRepositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Pokemon result = service.syncPokemon("35");

        assertThat(result.localizedName()).isNull();
        assertThat(result.region()).isNull();
        assertThat(result.tags()).isNull();
        assertThat(result.weight()).isEqualTo(75);
        verify(pokemonRepositoryPort).save(replica);
    }

    @Test
    void syncPokemon_existingPokemon_preservesProprietaryFieldsAndUpdatesReplicatedFields() {
        Pokemon existing = pokemon(35L, 60, "Fée-Fée", "Kanto", "starter,cute");
        Pokemon freshReplica = pokemon(35L, 80, null, null, null);
        when(pokeApiClientPort.fetchReplicaData("35")).thenReturn(freshReplica);
        when(pokemonRepositoryPort.findById(35L)).thenReturn(Optional.of(existing));
        when(pokemonRepositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Pokemon result = service.syncPokemon("35");

        assertThat(result.localizedName()).isEqualTo("Fée-Fée");
        assertThat(result.region()).isEqualTo("Kanto");
        assertThat(result.tags()).isEqualTo("starter,cute");
        assertThat(result.weight()).isEqualTo(80);
        verify(pokemonRepositoryPort).save(argThat(p ->
                "Fée-Fée".equals(p.localizedName()) && p.weight() == 80));
    }

    @Test
    void syncByIds_syncsEveryIdAndReturnsResults() {
        Pokemon replica1 = pokemon(1L, 10, null, null, null);
        Pokemon replica2 = pokemon(2L, 20, null, null, null);
        when(pokeApiClientPort.fetchReplicaData("1")).thenReturn(replica1);
        when(pokeApiClientPort.fetchReplicaData("2")).thenReturn(replica2);
        when(pokemonRepositoryPort.findById(any())).thenReturn(Optional.empty());
        when(pokemonRepositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Pokemon> result = service.syncByIds(List.of(1, 2));

        assertThat(result).containsExactly(replica1, replica2);
        verify(pokemonRepositoryPort).save(replica1);
        verify(pokemonRepositoryPort).save(replica2);
    }

    private Pokemon pokemon(long id, int weight, String localizedName, String region, String tags) {
        return new Pokemon(id, "clefairy", "sprite.png", "Fairy Pokémon", weight, 6,
                List.of("friend-guard"), List.of("pound"), List.of(new StatValue("speed", 35, 0)),
                List.of("fairy"), "desc", List.of(new EvolutionStage(173, "cleffa", null)),
                localizedName, region, tags);
    }
}
