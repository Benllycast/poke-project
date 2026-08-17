package com.bcastillo.pokeapiback.api.pokemon;

import com.bcastillo.pokeapiback.api.pokemon.mapper.PokemonDtoMapper;
import com.bcastillo.pokeapiback.application.pokemon.PokemonSyncService;
import com.bcastillo.pokeapiback.domain.exception.PokeApiResourceNotFoundException;
import com.bcastillo.pokeapiback.domain.model.EvolutionStage;
import com.bcastillo.pokeapiback.domain.model.Pokemon;
import com.bcastillo.pokeapiback.domain.model.StatValue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PokemonController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(PokemonDtoMapper.class)
class PokemonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PokemonSyncService syncService;

    @Test
    void sync_returnsPokemonResponse() throws Exception {
        Pokemon pokemon = new Pokemon(35L, "clefairy", "sprite.png", "Fairy Pokémon", 75, 6,
                List.of("friend-guard"), List.of("pound"), List.of(new StatValue("speed", 35, 0)),
                List.of("fairy"), "desc", List.of(new EvolutionStage(173, "cleffa", null)),
                null, null, null);
        when(syncService.syncPokemon(eq("35"))).thenReturn(pokemon);

        mockMvc.perform(post("/api/pokemon/sync/35"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(35))
                .andExpect(jsonPath("$.name").value("clefairy"))
                .andExpect(jsonPath("$.category").value("Fairy Pokémon"));
    }

    @Test
    void sync_pokeApiResourceNotFound_returns404() throws Exception {
        when(syncService.syncPokemon(eq("99999"))).thenThrow(new PokeApiResourceNotFoundException("99999"));

        mockMvc.perform(post("/api/pokemon/sync/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
