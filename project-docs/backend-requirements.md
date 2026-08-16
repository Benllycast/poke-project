# Backend Requirements

Technical/architectural requirements for `backend/pokeapi-back`. Business behavior lives in
`domain-requirements.md`; this covers the "how" on the backend side. Source: `requeriments.md`.

## Stack & Methodology

* Java + Spring Boot RESTful API.
* Clean Architecture: clear separation of concerns and independence between layers.
* TDD preferred as the development methodology.

## PokeAPI Integration

* Build a Spring Boot API that communicates with the external PokeAPI
  ([docs](https://pokeapi.co/docs/v2)) for retrieval of Pokemon data (see User Stories 01/02 in
  `domain-requirements.md`).
* Nice to have: caching layer for PokeAPI responses.

## API Layer

* Java Web API with comprehensive CRUD for the Pokemon dataset.
* Standard HTTP verbs, required parameters, and consistent response structures across endpoints.
* Auxiliary API for user registration and authentication, with a clear split between protected and
  public routes.

## Data Access Layer

* Dedicated layer to manage interactions with the persistence store, providing the foundational logic
  the API controllers rely on.

## Core Business Logic Layer

* Encapsulates domain rules and data validation.
* Must remain architecturally independent from both the API layer and the data access layer.

## Database

* Relational database (or equivalent) with:
  * Primary entity: Pokemon (see `domain-requirements.md` for required fields).
  * Secondary collection: Users, for auth/management.
* Both require a unique primary key and at least two descriptive attributes.

## Validation & Error Handling

* Local data modification (User Story 04) must return 404 for missing records and 400 for malformed
  payloads, plus additional defensive validation as needed.
* Proper error handling required throughout the API.

## Testing

* Include tests; thorough unit test coverage for every core component in the application suite.
* TDD is the preferred approach.

## Delivery

* Supply a Dockerfile so the backend can run containerized.
