# PokeAPI — Pokemon Section Spec

Source: https://pokeapi.co/docs/v2#pokemon-section
Base URL: `https://pokeapi.co/api/v2/`

## Endpoints

| Resource | Endpoint |
|---|---|
| Ability | `GET /ability/{id or name}/` |
| Characteristic | `GET /characteristic/{id}/` |
| Egg Group | `GET /egg-group/{id or name}/` |
| Gender | `GET /gender/{id or name}/` |
| Growth Rate | `GET /growth-rate/{id or name}/` |
| Nature | `GET /nature/{id or name}/` |
| Pokeathlon Stat | `GET /pokeathlon-stat/{id or name}/` |
| Pokemon | `GET /pokemon/{id or name}/` |
| Pokemon Location Areas | `GET /pokemon/{id or name}/encounters` |
| Pokemon Color | `GET /pokemon-color/{id or name}/` |
| Pokemon Form | `GET /pokemon-form/{id or name}/` |
| Pokemon Habitat | `GET /pokemon-habitat/{id or name}/` |
| Pokemon Shape | `GET /pokemon-shape/{id or name}/` |
| Pokemon Species | `GET /pokemon-species/{id or name}/` |
| Stat | `GET /stat/{id or name}/` |
| Type | `GET /type/{id or name}/` |

List calls (e.g. `GET /pokemon/`) accept `?limit=` and `?offset=` query params and return a paginated envelope (see below).

## Pagination envelope (NamedAPIResourceList)

| Field | Type | Description |
|---|---|---|
| count | integer | Total resources available |
| next | string \| null | URL of next page |
| previous | string \| null | URL of previous page |
| results | list\<NamedAPIResource\> | Page of `{ name, url }` items |

## Common referenced types

- **NamedAPIResource**: `{ name: string, url: string }`
- **APIResource**: `{ url: string }`
- **Name**: `{ name: string, language: NamedAPIResource }`
- **Description / Effect / FlavorText**: `{ text/effect/flavor_text: string, language: NamedAPIResource, version(_group)?: NamedAPIResource }`

## Pokemon (main resource)

`GET /pokemon/{id or name}/`

| Field | Type | Description |
|---|---|---|
| id | integer | Identifier |
| name | string | Name |
| base_experience | integer | XP awarded on defeat |
| height | integer | Height in decimetres |
| weight | integer | Weight in hectograms |
| is_default | boolean | Default variant flag |
| order | integer | Sort order for national dex UI |
| abilities | list\<PokemonAbility\> | Abilities this Pokemon can have |
| forms | list\<NamedAPIResource\> | Related Pokemon Form resources |
| game_indices | list\<VersionGameIndex\> | Internal game IDs per version |
| held_items | list\<PokemonHeldItem\> | Items it may hold in the wild |
| location_area_encounters | string | URL to encounter location list |
| moves | list\<PokemonMove\> | Moves it can learn |
| past_types | list\<PokemonTypePast\> | Historical typing in past generations |
| species | NamedAPIResource | Link to PokemonSpecies |
| sprites | PokemonSprites | Image resource set |
| cries | PokemonCries | Cry audio resource set |
| stats | list\<PokemonStat\> | Base stats |
| types | list\<PokemonType\> | Elemental type(s) |

### PokemonAbility
| Field | Type |
|---|---|
| is_hidden | boolean |
| slot | integer (1-3) |
| ability | NamedAPIResource → Ability |

### PokemonType
| Field | Type |
|---|---|
| slot | integer |
| type | NamedAPIResource → Type |

### PokemonTypePast
| Field | Type |
|---|---|
| generation | NamedAPIResource |
| types | list\<PokemonType\> |

### PokemonStat
| Field | Type |
|---|---|
| stat | NamedAPIResource → Stat |
| effort | integer |
| base_stat | integer |

### PokemonMove
| Field | Type |
|---|---|
| move | NamedAPIResource → Move |
| version_group_details | list\<PokemonMoveVersion\> |

**PokemonMoveVersion**: `{ move_learn_method: NamedAPIResource, version_group: NamedAPIResource, level_learned_at: integer, order: integer|null }`

### PokemonHeldItem
| Field | Type |
|---|---|
| item | NamedAPIResource → Item |
| version_details | list\<PokemonHeldItemVersion\> |

**PokemonHeldItemVersion**: `{ version: NamedAPIResource, rarity: integer }`

### VersionGameIndex
`{ game_index: integer, version: NamedAPIResource }`

### PokemonSprites
| Field | Type |
|---|---|
| front_default / front_shiny / front_female / front_shiny_female | string \| null |
| back_default / back_shiny / back_female / back_shiny_female | string \| null |
| other | object — dream_world, home, official-artwork, showdown sprite sets |
| versions | object — per-generation/per-game sprite sets |

### PokemonCries
`{ latest: string, legacy: string }` — URLs to cry audio files.

## Pokemon Species

`GET /pokemon-species/{id or name}/`

| Field | Type | Description |
|---|---|---|
| id | integer | Identifier |
| name | string | Name |
| order | integer | Sort order for national dex |
| gender_rate | integer | -1 (genderless) or eighths female (0-8) |
| capture_rate | integer | Base capture rate (0-255) |
| base_happiness | integer | Starting friendship value |
| is_baby | boolean | Baby Pokemon flag |
| is_legendary | boolean | Legendary flag |
| is_mythical | boolean | Mythical flag |
| hatch_counter | integer | Steps/256 to hatch |
| has_gender_differences | boolean | Visual gender differences exist |
| forms_switchable | boolean | Forms can be freely switched |
| growth_rate | NamedAPIResource → Growth Rate | Leveling curve |
| pokedex_numbers | list\<PokemonSpeciesDexEntry\> | Number per Pokedex |
| egg_groups | list\<NamedAPIResource\> | Breeding egg groups |
| color | NamedAPIResource → Pokemon Color | Pokedex color category |
| shape | NamedAPIResource → Pokemon Shape | Pokedex shape category |
| evolves_from_species | NamedAPIResource \| null | Pre-evolution |
| evolution_chain | APIResource | Link to evolution chain |
| habitat | NamedAPIResource \| null → Pokemon Habitat | Habitat |
| generation | NamedAPIResource | Generation introduced |
| names | list\<Name\> | Localized names |
| flavor_text_entries | list\<FlavorText\> | Pokedex flavor text per version |
| form_descriptions | list\<Description\> | Form differences description |
| genera | list\<Genus\> | Species genus per language (e.g. "Seed Pokemon") |
| varieties | list\<PokemonSpeciesVariety\> | Variant Pokemon of this species |

**PokemonSpeciesVariety**: `{ is_default: boolean, pokemon: NamedAPIResource }`
**PokemonSpeciesDexEntry**: `{ entry_number: integer, pokedex: NamedAPIResource }`
**Genus**: `{ genus: string, language: NamedAPIResource }`

## Type

`GET /type/{id or name}/`

| Field | Type | Description |
|---|---|---|
| id | integer | Identifier |
| name | string | Name |
| damage_relations | TypeRelations | Attack/defense multipliers vs other types |
| past_damage_relations | list\<TypeRelationsPast\> | Historical damage relations |
| game_indices | list\<GenerationGameIndex\> | Internal IDs per generation |
| generation | NamedAPIResource | Generation introduced |
| move_damage_class | NamedAPIResource \| null | physical / special / status |
| names | list\<Name\> | Localized names |
| pokemon | list\<TypePokemon\> | Pokemon with this type |
| moves | list\<NamedAPIResource\> | Moves of this type |

**TypeRelations**: `{ no_damage_to, half_damage_to, double_damage_to, no_damage_from, half_damage_from, double_damage_from: list<NamedAPIResource> }`
**TypePokemon**: `{ slot: integer, pokemon: NamedAPIResource }`
**TypeRelationsPast**: `{ generation: NamedAPIResource, damage_relations: TypeRelations }`

## Ability

`GET /ability/{id or name}/`

| Field | Type | Description |
|---|---|---|
| id | integer | Identifier |
| name | string | Name |
| is_main_series | boolean | Exists in main series games |
| generation | NamedAPIResource | Generation introduced |
| names | list\<Name\> | Localized names |
| effect_entries | list\<VerboseEffect\> | Effect description per language |
| effect_changes | list\<AbilityEffectChange\> | Effect changes across versions |
| flavor_text_entries | list\<AbilityFlavorText\> | Flavor text per version group |
| pokemon | list\<AbilityPokemon\> | Pokemon with this ability |

**AbilityPokemon**: `{ is_hidden: boolean, slot: integer, pokemon: NamedAPIResource }`

## Stat

`GET /stat/{id or name}/`

| Field | Type |
|---|---|
| id | integer |
| name | string |
| game_index | integer |
| is_battle_only | boolean |
| affecting_moves | object — increase/decrease move lists |
| affecting_natures | object — increase/decrease nature lists |
| characteristics | list\<APIResource\> |
| move_damage_class | NamedAPIResource \| null |
| names | list\<Name\> |

## Notes for this project

- No auth, no API key, rate-limited by fair-use policy — cache responses locally rather than re-fetching per request (matches CLAUDE.md's "caching is nice to have" and the local-replica requirement).
- Local `Pokemon` entity should replicate a subset of the above (id, name, sprites, types, stats at minimum) plus proprietary fields (localized name, region, tags) per the project's domain requirements — see [domain-requirements.md](domain-requirements.md).
- `id or name` path param means both numeric ID and slug name resolve to the same resource.
