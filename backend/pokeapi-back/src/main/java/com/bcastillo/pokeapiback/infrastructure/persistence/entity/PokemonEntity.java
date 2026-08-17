package com.bcastillo.pokeapiback.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "pokemon")
@Getter
public class PokemonEntity {

    @Id
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "sprite_url", length = 500)
    private String spriteUrl;

    @Column(length = 100)
    private String category;

    private Integer weight;
    private Integer height;

    @Lob
    @Column(name = "abilities_json")
    private String abilitiesJson;

    @Lob
    @Column(name = "moves_json")
    private String movesJson;

    @Lob
    @Column(name = "stats_json")
    private String statsJson;

    @Lob
    @Column(name = "types_json")
    private String typesJson;

    @Lob
    private String description;

    @Lob
    @Column(name = "evolution_chain_json")
    private String evolutionChainJson;

    @Column(name = "localized_name", length = 100)
    private String localizedName;

    @Column(length = 100)
    private String region;

    @Column(length = 500)
    private String tags;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PokemonEntity() {
    }

    public PokemonEntity(Long id, String name, String spriteUrl, String category, Integer weight,
                          Integer height, String abilitiesJson, String movesJson, String statsJson,
                          String typesJson, String description, String evolutionChainJson,
                          String localizedName, String region, String tags) {
        this.id = id;
        this.name = name;
        this.spriteUrl = spriteUrl;
        this.category = category;
        this.weight = weight;
        this.height = height;
        this.abilitiesJson = abilitiesJson;
        this.movesJson = movesJson;
        this.statsJson = statsJson;
        this.typesJson = typesJson;
        this.description = description;
        this.evolutionChainJson = evolutionChainJson;
        this.localizedName = localizedName;
        this.region = region;
        this.tags = tags;
    }
}
