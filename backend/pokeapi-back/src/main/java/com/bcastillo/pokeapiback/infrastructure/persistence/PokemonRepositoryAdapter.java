package com.bcastillo.pokeapiback.infrastructure.persistence;

import com.bcastillo.pokeapiback.domain.model.Pokemon;
import com.bcastillo.pokeapiback.domain.port.PokemonRepositoryPort;
import com.bcastillo.pokeapiback.infrastructure.persistence.entity.PokemonEntity;
import com.bcastillo.pokeapiback.infrastructure.persistence.mapper.PokemonEntityMapper;
import com.bcastillo.pokeapiback.infrastructure.persistence.repository.PokemonJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PokemonRepositoryAdapter implements PokemonRepositoryPort {

    private final PokemonJpaRepository jpaRepository;
    private final PokemonEntityMapper mapper;

    public PokemonRepositoryAdapter(PokemonJpaRepository jpaRepository, PokemonEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Pokemon> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Pokemon save(Pokemon pokemon) {
        PokemonEntity saved = jpaRepository.save(mapper.toEntity(pokemon));
        return mapper.toDomain(saved);
    }
}
