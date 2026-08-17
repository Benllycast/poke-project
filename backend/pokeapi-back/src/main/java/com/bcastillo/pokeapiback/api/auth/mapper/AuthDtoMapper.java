package com.bcastillo.pokeapiback.api.auth.mapper;

import com.bcastillo.pokeapiback.api.auth.dto.AuthResponse;
import org.springframework.stereotype.Component;

@Component
public class AuthDtoMapper {

    public AuthResponse toResponse(String token, String email) {
        return new AuthResponse(token, email);
    }
}
