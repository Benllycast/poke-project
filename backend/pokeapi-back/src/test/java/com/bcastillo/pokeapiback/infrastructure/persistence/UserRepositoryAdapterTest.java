package com.bcastillo.pokeapiback.infrastructure.persistence;

import com.bcastillo.pokeapiback.domain.model.User;
import com.bcastillo.pokeapiback.domain.model.UserRole;
import com.bcastillo.pokeapiback.infrastructure.persistence.mapper.UserEntityMapper;
import com.bcastillo.pokeapiback.infrastructure.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class UserRepositoryAdapterTest {

    @Autowired
    private UserJpaRepository jpaRepository;

    private UserRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new UserRepositoryAdapter(jpaRepository, new UserEntityMapper());
    }

    @Test
    void save_thenFindByEmail_roundTripsFields() {
        User user = new User(null, "trainer@example.com", "hashed-password", UserRole.USER);

        User saved = adapter.save(user);
        Optional<User> reloaded = adapter.findByEmail("trainer@example.com");

        assertThat(saved.id()).isNotNull();
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().email()).isEqualTo("trainer@example.com");
        assertThat(reloaded.get().passwordHash()).isEqualTo("hashed-password");
        assertThat(reloaded.get().role()).isEqualTo(UserRole.USER);
    }

    @Test
    void findByEmail_returnsEmptyWhenMissing() {
        assertThat(adapter.findByEmail("nobody@example.com")).isEmpty();
    }

    @Test
    void save_duplicateEmail_violatesUniqueConstraint() {
        adapter.save(new User(null, "duplicate@example.com", "hash1", UserRole.USER));
        jpaRepository.flush();

        assertThatThrownBy(() -> {
            adapter.save(new User(null, "duplicate@example.com", "hash2", UserRole.USER));
            jpaRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
