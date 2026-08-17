package com.bcastillo.pokeapiback.domain.port;

import com.bcastillo.pokeapiback.domain.model.User;

import java.util.Optional;

public interface UserRepositoryPort {

    Optional<User> findByEmail(String email);

    User save(User user);
}
