package com.bcastillo.pokeapiback.api.auth;

import com.bcastillo.pokeapiback.api.auth.dto.AuthResponse;
import com.bcastillo.pokeapiback.api.auth.dto.LoginRequest;
import com.bcastillo.pokeapiback.api.auth.dto.RegisterRequest;
import com.bcastillo.pokeapiback.api.auth.mapper.AuthDtoMapper;
import com.bcastillo.pokeapiback.application.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthDtoMapper mapper;

    public AuthController(AuthService authService, AuthDtoMapper mapper) {
        this.authService = authService;
        this.mapper = mapper;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        String token = authService.register(request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(token, request.email()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.email(), request.password());
        return ResponseEntity.ok(mapper.toResponse(token, request.email()));
    }
}
