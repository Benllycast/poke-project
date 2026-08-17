package com.bcastillo.pokeapiback.application.auth;

import com.bcastillo.pokeapiback.domain.exception.InvalidCredentialsException;
import com.bcastillo.pokeapiback.domain.exception.UserAlreadyExistsException;
import com.bcastillo.pokeapiback.domain.model.User;
import com.bcastillo.pokeapiback.domain.model.UserRole;
import com.bcastillo.pokeapiback.domain.port.PasswordHasher;
import com.bcastillo.pokeapiback.domain.port.TokenProvider;
import com.bcastillo.pokeapiback.domain.port.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private TokenProvider tokenProvider;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(userRepositoryPort, passwordHasher, tokenProvider);
    }

    @Test
    void register_newEmail_savesHashedPasswordAndReturnsToken() {
        when(userRepositoryPort.findByEmail("trainer@example.com")).thenReturn(Optional.empty());
        when(passwordHasher.hash("Password123!")).thenReturn("hashed");
        User saved = new User(1L, "trainer@example.com", "hashed", UserRole.USER);
        when(userRepositoryPort.save(any())).thenReturn(saved);
        when(tokenProvider.generateToken(saved)).thenReturn("jwt-token");

        String token = service.register("trainer@example.com", "Password123!");

        assertThat(token).isEqualTo("jwt-token");
        verify(userRepositoryPort).save(new User(null, "trainer@example.com", "hashed", UserRole.USER));
    }

    @Test
    void register_existingEmail_throwsUserAlreadyExistsException() {
        when(userRepositoryPort.findByEmail("trainer@example.com"))
                .thenReturn(Optional.of(new User(1L, "trainer@example.com", "hashed", UserRole.USER)));

        assertThatThrownBy(() -> service.register("trainer@example.com", "Password123!"))
                .isInstanceOf(UserAlreadyExistsException.class);
        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    void login_correctPassword_returnsToken() {
        User user = new User(1L, "trainer@example.com", "hashed", UserRole.USER);
        when(userRepositoryPort.findByEmail("trainer@example.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("Password123!", "hashed")).thenReturn(true);
        when(tokenProvider.generateToken(user)).thenReturn("jwt-token");

        assertThat(service.login("trainer@example.com", "Password123!")).isEqualTo("jwt-token");
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentialsException() {
        User user = new User(1L, "trainer@example.com", "hashed", UserRole.USER);
        when(userRepositoryPort.findByEmail("trainer@example.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("wrong-password", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> service.login("trainer@example.com", "wrong-password"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_unknownEmail_throwsInvalidCredentialsException() {
        when(userRepositoryPort.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login("nobody@example.com", "Password123!"))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
