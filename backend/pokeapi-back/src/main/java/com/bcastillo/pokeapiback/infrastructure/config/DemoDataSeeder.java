package com.bcastillo.pokeapiback.infrastructure.config;

import com.bcastillo.pokeapiback.application.auth.AuthService;
import com.bcastillo.pokeapiback.application.pokemon.PokemonSyncService;
import com.bcastillo.pokeapiback.domain.exception.UserAlreadyExistsException;
import com.bcastillo.pokeapiback.domain.port.PokemonRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.IntStream;

@Component
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final AuthService authService;
    private final PokemonSyncService pokemonSyncService;
    private final PokemonRepositoryPort pokemonRepositoryPort;
    private final boolean enabled;
    private final int pokemonCount;
    private final String demoEmail;
    private final String demoPassword;

    public DemoDataSeeder(AuthService authService, PokemonSyncService pokemonSyncService,
                           PokemonRepositoryPort pokemonRepositoryPort,
                           @Value("${app.seed.enabled}") boolean enabled,
                           @Value("${app.seed.pokemon-count}") int pokemonCount,
                           @Value("${app.seed.demo-email}") String demoEmail,
                           @Value("${app.seed.demo-password}") String demoPassword) {
        this.authService = authService;
        this.pokemonSyncService = pokemonSyncService;
        this.pokemonRepositoryPort = pokemonRepositoryPort;
        this.enabled = enabled;
        this.pokemonCount = pokemonCount;
        this.demoEmail = demoEmail;
        this.demoPassword = demoPassword;
    }

    @Override
    public void run(String... args) {
        if (!enabled) {
            return;
        }
        seedDemoUser();
        seedPokemon();
    }

    private void seedDemoUser() {
        try {
            authService.register(demoEmail, demoPassword);
            log.info("Seeded demo user {} / {}", demoEmail, demoPassword);
        } catch (UserAlreadyExistsException e) {
            log.debug("Demo user {} already exists, skipping seed", demoEmail);
        }
    }

    private void seedPokemon() {
        if (pokemonRepositoryPort.findAll(0, 1).totalElements() > 0) {
            log.debug("Pokemon table already has data, skipping seed");
            return;
        }
        List<Integer> ids = IntStream.rangeClosed(1, pokemonCount).boxed().toList();
        pokemonSyncService.syncByIds(ids);
        log.info("Seeded {} Pokemon", pokemonCount);
    }
}
