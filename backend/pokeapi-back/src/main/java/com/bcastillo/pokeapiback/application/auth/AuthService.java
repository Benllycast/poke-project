package com.bcastillo.pokeapiback.application.auth;

import com.bcastillo.pokeapiback.domain.exception.InvalidCredentialsException;
import com.bcastillo.pokeapiback.domain.exception.UserAlreadyExistsException;
import com.bcastillo.pokeapiback.domain.model.User;
import com.bcastillo.pokeapiback.domain.model.UserRole;
import com.bcastillo.pokeapiback.domain.port.PasswordHasher;
import com.bcastillo.pokeapiback.domain.port.TokenProvider;
import com.bcastillo.pokeapiback.domain.port.UserRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;

    public AuthService(UserRepositoryPort userRepositoryPort, PasswordHasher passwordHasher,
                        TokenProvider tokenProvider) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
    }

    public String register(String email, String rawPassword) {
        if (userRepositoryPort.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException(email);
        }
        User user = new User(null, email, passwordHasher.hash(rawPassword), UserRole.USER);
        User saved = userRepositoryPort.save(user);
        return tokenProvider.generateToken(saved);
    }

    public String login(String email, String rawPassword) {
        User user = userRepositoryPort.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordHasher.matches(rawPassword, user.passwordHash())) {
            throw new InvalidCredentialsException();
        }
        return tokenProvider.generateToken(user);
    }
}
