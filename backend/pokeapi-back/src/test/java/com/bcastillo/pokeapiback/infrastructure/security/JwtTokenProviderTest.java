package com.bcastillo.pokeapiback.infrastructure.security;

import com.bcastillo.pokeapiback.domain.model.User;
import com.bcastillo.pokeapiback.domain.model.UserRole;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-key-at-least-32-bytes-long-1234567890";

    @Test
    void generateThenParse_recoversEmailAndRoleClaims() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 60_000);
        User user = new User(1L, "trainer@example.com", "hashed", UserRole.ADMIN);

        String token = provider.generateToken(user);
        Jws<Claims> parsed = provider.parse(token);

        assertThat(parsed.getPayload().getSubject()).isEqualTo("trainer@example.com");
        assertThat(parsed.getPayload().get("role", String.class)).isEqualTo("ADMIN");
    }

    @Test
    void parse_expiredToken_throwsExpiredJwtException() throws InterruptedException {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 1);
        User user = new User(1L, "trainer@example.com", "hashed", UserRole.USER);
        String token = provider.generateToken(user);

        Thread.sleep(50);

        assertThatThrownBy(() -> provider.parse(token)).isInstanceOf(ExpiredJwtException.class);
    }
}
