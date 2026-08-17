package com.bcastillo.pokeapiback.api.pokemon;

import com.bcastillo.pokeapiback.api.pokemon.dto.PokemonPageResponse;
import com.bcastillo.pokeapiback.api.pokemon.dto.PokemonResponse;
import com.bcastillo.pokeapiback.api.pokemon.mapper.PokemonDtoMapper;
import com.bcastillo.pokeapiback.application.pokemon.PokemonQueryService;
import com.bcastillo.pokeapiback.application.pokemon.PokemonSyncService;
import com.bcastillo.pokeapiback.domain.model.PageResult;
import com.bcastillo.pokeapiback.domain.model.Pokemon;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pokemon")
public class PokemonController {

    private final PokemonSyncService syncService;
    private final PokemonQueryService queryService;
    private final PokemonDtoMapper mapper;

    public PokemonController(PokemonSyncService syncService, PokemonQueryService queryService,
                              PokemonDtoMapper mapper) {
        this.syncService = syncService;
        this.queryService = queryService;
        this.mapper = mapper;
    }

    @PostMapping("/sync/{idOrName}")
    public ResponseEntity<PokemonResponse> sync(@PathVariable String idOrName) {
        Pokemon pokemon = syncService.syncPokemon(idOrName);
        return ResponseEntity.ok(mapper.toResponse(pokemon));
    }

    @GetMapping
    public ResponseEntity<PokemonPageResponse> list(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        PageResult<Pokemon> result = queryService.list(page, size);
        return ResponseEntity.ok(mapper.toPageResponse(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PokemonResponse> getById(@PathVariable Long id) {
        Pokemon pokemon = queryService.getById(id);
        return ResponseEntity.ok(mapper.toResponse(pokemon));
    }
}
