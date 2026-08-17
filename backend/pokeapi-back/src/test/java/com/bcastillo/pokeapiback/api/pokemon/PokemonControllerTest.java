package com.bcastillo.pokeapiback.api.pokemon;

import com.bcastillo.pokeapiback.api.pokemon.mapper.PokemonDtoMapper;
import com.bcastillo.pokeapiback.application.pokemon.PokemonQueryService;
import com.bcastillo.pokeapiback.application.pokemon.PokemonSyncService;
import com.bcastillo.pokeapiback.domain.exception.PokeApiResourceNotFoundException;
import com.bcastillo.pokeapiback.domain.exception.PokemonNotFoundException;
import com.bcastillo.pokeapiback.domain.model.EvolutionStage;
import com.bcastillo.pokeapiback.domain.model.PageResult;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @MockitoBean
    private PokemonQueryService queryService;

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

    @Test
    void list_returnsPokemonPageResponse() throws Exception {
        Pokemon pokemon = pokemon(35L);
        PageResult<Pokemon> page = new PageResult<>(List.of(pokemon), 0, 20, 1, 1);
        when(queryService.list(0, 20)).thenReturn(page);

        mockMvc.perform(get("/api/pokemon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(35))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void getById_found_returnsPokemonResponse() throws Exception {
        when(queryService.getById(35L)).thenReturn(pokemon(35L));

        mockMvc.perform(get("/api/pokemon/35"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(35))
                .andExpect(jsonPath("$.name").value("clefairy"));
    }

    @Test
    void getById_missing_returns404() throws Exception {
        when(queryService.getById(9999L)).thenThrow(new PokemonNotFoundException(9999L));

        mockMvc.perform(get("/api/pokemon/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private Pokemon pokemon(long id) {
        return new Pokemon(id, "clefairy", "sprite.png", "Fairy Pokémon", 75, 6,
                List.of("friend-guard"), List.of("pound"), List.of(new StatValue("speed", 35, 0)),
                List.of("fairy"), "desc", List.of(new EvolutionStage(173, "cleffa", null)),
                null, null, null);
    }
}
