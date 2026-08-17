import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import * as pokemonApi from '../api/pokemonApi.js'
import { useAuth } from '../context/AuthContext.jsx'

function SyncForm() {
  const { isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [idOrName, setIdOrName] = useState('')

  const mutation = useMutation({
    mutationFn: () => pokemonApi.sync(idOrName),
    onSuccess: (pokemon) => {
      queryClient.invalidateQueries({ queryKey: ['pokemon', 'list'] })
      setIdOrName('')
      navigate(`/pokemon/${pokemon.id}`)
    },
  })

  if (!isAuthenticated) {
    return null
  }

  function handleSubmit(event) {
    event.preventDefault()
    if (idOrName.trim()) {
      mutation.mutate()
    }
  }

  return (
    <form className="sync-form" onSubmit={handleSubmit}>
      <label>
        Sync Pokemon by id or name
        <input
          value={idOrName}
          onChange={(event) => setIdOrName(event.target.value)}
          placeholder="e.g. 25 or pikachu"
        />
      </label>
      <button type="submit" disabled={mutation.isPending}>
        Sync
      </button>
      {mutation.isError && <p role="alert">{mutation.error.message}</p>}
    </form>
  )
}

export default SyncForm
