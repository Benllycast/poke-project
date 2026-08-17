package com.bcastillo.pokeapiback.infrastructure.persistence.mapper;

import com.bcastillo.pokeapiback.domain.model.User;
import com.bcastillo.pokeapiback.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserEntityMapper {

    public UserEntity toEntity(User user) {
        return new UserEntity(user.email(), user.passwordHash(), user.role());
    }

    public User toDomain(UserEntity entity) {
        return new User(entity.getId(), entity.getEmail(), entity.getPasswordHash(), entity.getRole());
    }
}
