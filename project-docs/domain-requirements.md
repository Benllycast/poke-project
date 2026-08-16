# Domain Requirements

Business/functional requirements — the "what," independent of backend or frontend implementation.
Source: `requeriments.md`.

## Project Overview

Build a solution, backed by Java/Spring Boot on the backend, that interfaces with the external
[PokeAPI](https://pokeapi.co/docs/v2) to retrieve, locally replicate, and allow attribute modification
of Pokemon entries.

## Entities

* **Pokemon** (primary entity) — replicated from PokeAPI, extended with proprietary fields not present
  upstream (e.g. localized name, geographical metadata, internal classification tags). Requires a unique
  primary key and at least two descriptive attributes.
* **User** (secondary entity) — supports registration/authentication and distinguishes protected vs.
  public access. Requires a unique primary key and at least two descriptive attributes.

## User Story 01: Pokemon Enumeration

* Browse Pokemon via paginated results.
* Each entry displays: sprite, category, mass, and its collection of skills (abilities/moves).

## User Story 02: Detailed View

* View comprehensive data for a chosen Pokemon: image, core statistics, narrative description, and
  evolutionary lineage.

## User Story 03: Data Synchronization

* Persist Pokemon data into a local relational store.
* This replication layer must support adding proprietary fields not available from PokeAPI (localized
  nomenclature, geographical metadata, internal classification tags, etc.).

## User Story 04: Local Data Modification

* Allow updates to any Pokemon currently stored in the local database.
* Validation rules: 404 for missing records, 400 for malformed payloads, plus further defensive logic as
  needed.

## Delivery (project-level, not layer-specific)

* Host the code in a public Git repository.
* Provide a comprehensive README covering environment setup and technical documentation.
* Pre-populate the application with seeded data / mock credentials for demonstration.
* Supply a Dockerfile for containerized execution.

## GenAI Tools Exercise (separate written deliverable)

Imagine generating a RESTful **task management** API (unrelated to the Pokemon app) supporting CRUD on
tasks (`title`, `description`, `status`, `due_date`), each associated with a user (assume a basic User
model exists). Using a preferred GenAI coding tool (Cursor, Claude Code, Windsurf, GitHub Copilot, etc.):

* Write the prompt you would use to generate the API scaffold or full implementation.
* Show the output code (or a representative sample).
* Describe how you validated the AI's suggestions, corrected/improved the output, and handled edge
  cases, authentication, or validations.

## Presentation and Code Review

* Present the project to the technical interview panel over Google Meet/Zoom, screen-sharing the GitHub
  repository or IDE: explain the user stories, design choices, technical architecture, and demonstrate
  functionality.
* A code review follows the presentation; be ready to explain coding decisions.

Evaluation criteria:

* **Clean Architecture** — separation of concerns and independence of components.
* **Application testing** — sufficient coverage; TDD preferred.
* **Code quality** — well-organized, readable, follows best practices.
* **Functionality** — works as specified, no errors/bugs; no browser console warnings is a plus.
* **Presentation** — clear, concise, demonstrates understanding of backend and frontend best practices.
* **GenAI tools** — fluency with GenAI tools/prompt engineering and critical thinking about AI output.
