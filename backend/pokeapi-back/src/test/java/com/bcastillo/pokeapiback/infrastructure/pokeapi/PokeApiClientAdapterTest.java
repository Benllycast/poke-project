package com.bcastillo.pokeapiback.infrastructure.pokeapi;

import com.bcastillo.pokeapiback.domain.exception.PokeApiResourceNotFoundException;
import com.bcastillo.pokeapiback.domain.model.EvolutionStage;
import com.bcastillo.pokeapiback.domain.model.Pokemon;
import com.bcastillo.pokeapiback.infrastructure.pokeapi.mapper.PokeApiResponseMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PokeApiClientAdapterTest {

    private static final Path FIXTURES = Path.of("src/test/resources/wiremock");

    private WireMockServer wireMockServer;
    private PokeApiClientAdapter adapter;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
        RestClient restClient = RestClient.builder().baseUrl(wireMockServer.baseUrl()).build();
        adapter = new PokeApiClientAdapter(restClient, new PokeApiResponseMapper());
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void fetchReplicaData_returnsPopulatedPokemon() throws Exception {
        stub("/pokemon/35", "pokemon-response.json");
        stub("/pokemon-species/35", "pokemon-species-response.json");
        stub("/evolution-chain/49", "evolution-chain-response.json");

        Pokemon result = adapter.fetchReplicaData("35");

        assertThat(result.id()).isEqualTo(35L);
        assertThat(result.name()).isEqualTo("clefairy");
        assertThat(result.category()).isEqualTo("Fairy Pokémon");
        assertThat(result.abilities()).containsExactly("friend-guard");
        assertThat(result.evolutionChain()).containsExactly(
                new EvolutionStage(173, "cleffa", null),
                new EvolutionStage(35, "clefairy", 16),
                new EvolutionStage(36, "clefable", 30)
        );
        assertThat(result.localizedName()).isNull();
    }

    @Test
    void fetchReplicaData_throwsWhenPokemonNotFound() {
        wireMockServer.stubFor(get(urlEqualTo("/pokemon/99999"))
                .willReturn(aResponse().withStatus(404)));

        assertThrows(PokeApiResourceNotFoundException.class, () -> adapter.fetchReplicaData("99999"));
    }

    private void stub(String path, String fixtureFile) throws Exception {
        String body = Files.readString(FIXTURES.resolve(fixtureFile));
        wireMockServer.stubFor(get(urlEqualTo(path)).willReturn(okJson(body)));
    }
}
