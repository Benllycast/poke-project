package com.bcastillo.pokeapiback.api.pokemon;

import com.bcastillo.pokeapiback.api.pokemon.dto.PokemonCreateRequest;
import com.bcastillo.pokeapiback.api.pokemon.dto.PokemonPageResponse;
import com.bcastillo.pokeapiback.api.pokemon.dto.PokemonResponse;
import com.bcastillo.pokeapiback.api.pokemon.dto.PokemonUpdateRequest;
import com.bcastillo.pokeapiback.api.pokemon.mapper.PokemonDtoMapper;
import com.bcastillo.pokeapiback.application.pokemon.PokemonEditService;
import com.bcastillo.pokeapiback.application.pokemon.PokemonQueryService;
import com.bcastillo.pokeapiback.application.pokemon.PokemonSyncService;
import com.bcastillo.pokeapiback.domain.model.PageResult;
import com.bcastillo.pokeapiback.domain.model.Pokemon;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pokemon")
public class PokemonController {

    private final PokemonSyncService syncService;
    private final PokemonQueryService queryService;
    private final PokemonEditService editService;
    private final PokemonDtoMapper mapper;

    public PokemonController(PokemonSyncService syncService, PokemonQueryService queryService,
                              PokemonEditService editService, PokemonDtoMapper mapper) {
        this.syncService = syncService;
        this.queryService = queryService;
        this.editService = editService;
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

    @PostMapping
    public ResponseEntity<PokemonResponse> create(@Valid @RequestBody PokemonCreateRequest request) {
        Pokemon created = editService.create(mapper.fromCreateRequest(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PokemonResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody PokemonUpdateRequest request) {
        Pokemon updated = editService.update(id, mapper.applyUpdate(id, request));
        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        editService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
