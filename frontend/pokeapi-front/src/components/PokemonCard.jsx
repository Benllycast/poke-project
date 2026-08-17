import { Link } from 'react-router-dom'

function PokemonCard({ pokemon }) {
  const skills = [...(pokemon.abilities ?? []), ...(pokemon.moves ?? [])]

  return (
    <Link className="pokemon-card" to={`/pokemon/${pokemon.id}`}>
      {pokemon.spriteUrl && (
        <img src={pokemon.spriteUrl} alt={pokemon.name} width={96} height={96} />
      )}
      <h3>{pokemon.name}</h3>
      {pokemon.category && <p className="pokemon-card-category">{pokemon.category}</p>}
      {pokemon.weight != null && <p>Mass: {pokemon.weight}</p>}
      {skills.length > 0 && (
        <ul className="pokemon-card-skills">
          {skills.map((skill) => (
            <li key={skill}>{skill}</li>
          ))}
        </ul>
      )}
    </Link>
  )
}

export default PokemonCard
