import { Link, useNavigate, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import * as pokemonApi from '../api/pokemonApi.js'
import PokemonForm from '../components/PokemonForm.jsx'

function PokemonEditPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['pokemon', 'detail', id],
    queryFn: () => pokemonApi.getById(id),
  })

  const mutation = useMutation({
    mutationFn: (payload) => pokemonApi.update(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pokemon', 'detail', id] })
      queryClient.invalidateQueries({ queryKey: ['pokemon', 'list'] })
      navigate(`/pokemon/${id}`)
    },
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

  function handleSubmit(formValues) {
    mutation.mutate({
      name: formValues.name,
      spriteUrl: data.spriteUrl,
      category: data.category,
      weight: formValues.weight,
      height: formValues.height,
      abilities: data.abilities,
      moves: data.moves,
      stats: data.stats,
      types: data.types,
      description: data.description,
      evolutionChain: data.evolutionChain,
      localizedName: formValues.localizedName,
      region: formValues.region,
      tags: formValues.tags,
    })
  }

  return (
    <section>
      <p>
        <Link to={`/pokemon/${id}`}>&larr; Back to detail</Link>
      </p>
      <h1>Edit {data.name}</h1>
      <PokemonForm initialValues={data} onSubmit={handleSubmit} submitting={mutation.isPending} />
      {mutation.isError && <p role="alert">{mutation.error.message}</p>}
    </section>
  )
}

export default PokemonEditPage
