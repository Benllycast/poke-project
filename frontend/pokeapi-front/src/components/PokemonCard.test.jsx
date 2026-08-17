import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it } from 'vitest'
import PokemonCard from './PokemonCard.jsx'

const pokemon = {
  id: 35,
  name: 'clefairy',
  spriteUrl: 'clefairy.png',
  category: 'Fairy Pokémon',
  weight: 75,
  height: 6,
  abilities: ['friend-guard'],
  moves: ['pound'],
}

describe('PokemonCard', () => {
  it('renders sprite, category, mass, and skills', () => {
    render(
      <MemoryRouter>
        <PokemonCard pokemon={pokemon} />
      </MemoryRouter>,
    )

    expect(screen.getByRole('img', { name: 'clefairy' })).toHaveAttribute('src', 'clefairy.png')
    expect(screen.getByText('clefairy')).toBeInTheDocument()
    expect(screen.getByText('Fairy Pokémon')).toBeInTheDocument()
    expect(screen.getByText('Mass: 75')).toBeInTheDocument()
    expect(screen.getByText('friend-guard')).toBeInTheDocument()
    expect(screen.getByText('pound')).toBeInTheDocument()
  })

  it('links to the detail page', () => {
    render(
      <MemoryRouter>
        <PokemonCard pokemon={pokemon} />
      </MemoryRouter>,
    )

    expect(screen.getByRole('link')).toHaveAttribute('href', '/pokemon/35')
  })
})
