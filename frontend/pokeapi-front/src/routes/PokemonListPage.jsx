import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import * as pokemonApi from '../api/pokemonApi.js'
import PokemonCard from '../components/PokemonCard.jsx'
import Pagination from '../components/Pagination.jsx'

const PAGE_SIZE = 20

function PokemonListPage() {
  const [page, setPage] = useState(0)
  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['pokemon', 'list', page, PAGE_SIZE],
    queryFn: () => pokemonApi.list(page, PAGE_SIZE),
  })

  if (isLoading) {
    return <p>Loading Pokemon...</p>
  }

  if (isError) {
    return <p role="alert">Failed to load Pokemon: {error.message}</p>
  }

  return (
    <section>
      <h1>Pokedex</h1>
      {data.items.length === 0 ? (
        <p>No Pokemon yet. Sync one to get started.</p>
      ) : (
        <div className="pokemon-grid">
          {data.items.map((pokemon) => (
            <PokemonCard key={pokemon.id} pokemon={pokemon} />
          ))}
        </div>
      )}
      <Pagination page={data.page} totalPages={data.totalPages} onPageChange={setPage} />
    </section>
  )
}

export default PokemonListPage
