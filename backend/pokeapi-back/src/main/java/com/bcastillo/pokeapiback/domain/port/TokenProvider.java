package com.bcastillo.pokeapiback.domain.port;

import com.bcastillo.pokeapiback.domain.model.User;

public interface TokenProvider {

    String generateToken(User user);
}
