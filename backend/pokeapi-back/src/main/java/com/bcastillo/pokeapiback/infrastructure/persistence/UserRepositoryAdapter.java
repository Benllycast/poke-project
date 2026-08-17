package com.bcastillo.pokeapiback.infrastructure.persistence;

import com.bcastillo.pokeapiback.domain.model.User;
import com.bcastillo.pokeapiback.domain.port.UserRepositoryPort;
import com.bcastillo.pokeapiback.infrastructure.persistence.entity.UserEntity;
import com.bcastillo.pokeapiback.infrastructure.persistence.mapper.UserEntityMapper;
import com.bcastillo.pokeapiback.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository jpaRepository;
    private final UserEntityMapper mapper;

    public UserRepositoryAdapter(UserJpaRepository jpaRepository, UserEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity saved = jpaRepository.save(mapper.toEntity(user));
        return mapper.toDomain(saved);
    }
}
