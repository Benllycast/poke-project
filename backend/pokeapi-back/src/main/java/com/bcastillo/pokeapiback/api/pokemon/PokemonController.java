package com.bcastillo.pokeapiback.api.pokemon;

import com.bcastillo.pokeapiback.api.pokemon.dto.PokemonResponse;
import com.bcastillo.pokeapiback.api.pokemon.mapper.PokemonDtoMapper;
import com.bcastillo.pokeapiback.application.pokemon.PokemonSyncService;
import com.bcastillo.pokeapiback.domain.model.Pokemon;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pokemon")
public class PokemonController {

    private final PokemonSyncService syncService;
    private final PokemonDtoMapper mapper;

    public PokemonController(PokemonSyncService syncService, PokemonDtoMapper mapper) {
        this.syncService = syncService;
        this.mapper = mapper;
    }

    @PostMapping("/sync/{idOrName}")
    public ResponseEntity<PokemonResponse> sync(@PathVariable String idOrName) {
        Pokemon pokemon = syncService.syncPokemon(idOrName);
        return ResponseEntity.ok(mapper.toResponse(pokemon));
    }
}
