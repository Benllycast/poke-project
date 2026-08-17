import { Link, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import * as pokemonApi from '../api/pokemonApi.js'
import StatBar from '../components/StatBar.jsx'
import EvolutionChain from '../components/EvolutionChain.jsx'

function PokemonDetailPage() {
  const { id } = useParams()
  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['pokemon', 'detail', id],
    queryFn: () => pokemonApi.getById(id),
  })

  if (isLoading) {
    return <p>Loading Pokemon...</p>
  }

  if (isError) {
    if (error.status === 404) {
      return <p role="alert">Pokemon not found.</p>
    }
    return <p role="alert">Failed to load Pokemon: {error.message}</p>
  }

  return (
    <article>
      <p>
        <Link to="/">&larr; Back to list</Link>
      </p>
      <h1>{data.name}</h1>
      {data.spriteUrl && <img src={data.spriteUrl} alt={data.name} width={192} height={192} />}
      {data.category && <p className="pokemon-detail-category">{data.category}</p>}
      {data.description && <p>{data.description}</p>}
      {data.stats?.length > 0 && (
        <section>
          <h2>Stats</h2>
          {data.stats.map((stat) => (
            <StatBar key={stat.name} stat={stat} />
          ))}
        </section>
      )}
      {data.evolutionChain?.length > 0 && (
        <section>
          <h2>Evolution</h2>
          <EvolutionChain stages={data.evolutionChain} />
        </section>
      )}
    </article>
  )
}

export default PokemonDetailPage
