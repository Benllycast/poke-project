package com.bcastillo.pokeapiback.infrastructure.persistence;

import com.bcastillo.pokeapiback.domain.model.PageResult;
import com.bcastillo.pokeapiback.domain.model.Pokemon;
import com.bcastillo.pokeapiback.domain.port.PokemonRepositoryPort;
import com.bcastillo.pokeapiback.infrastructure.persistence.entity.PokemonEntity;
import com.bcastillo.pokeapiback.infrastructure.persistence.mapper.PokemonEntityMapper;
import com.bcastillo.pokeapiback.infrastructure.persistence.repository.PokemonJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
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

    @Override
    public PageResult<Pokemon> findAll(int page, int size) {
        Page<PokemonEntity> result = jpaRepository.findAll(PageRequest.of(page, size));
        List<Pokemon> items = result.getContent().stream().map(mapper::toDomain).toList();
        return new PageResult<>(items, page, size, result.getTotalElements(), result.getTotalPages());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
