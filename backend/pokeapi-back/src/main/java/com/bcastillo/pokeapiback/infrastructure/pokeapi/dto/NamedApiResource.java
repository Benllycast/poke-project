package com.bcastillo.pokeapiback.infrastructure.pokeapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NamedApiResource {
    public String name;
    public String url;
}
