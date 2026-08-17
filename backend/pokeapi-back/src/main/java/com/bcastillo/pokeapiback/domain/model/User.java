package com.bcastillo.pokeapiback.domain.model;

public record User(Long id, String email, String passwordHash, UserRole role) {
}
