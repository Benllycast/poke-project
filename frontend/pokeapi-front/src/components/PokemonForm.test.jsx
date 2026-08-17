import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import PokemonForm from './PokemonForm.jsx'

const initialValues = {
  name: 'clefairy',
  weight: 75,
  height: 6,
  localizedName: '',
  region: '',
  tags: '',
}

describe('PokemonForm', () => {
  it('pre-fills fields from the existing record', () => {
    render(<PokemonForm initialValues={initialValues} onSubmit={() => {}} />)

    expect(screen.getByLabelText('Name')).toHaveValue('clefairy')
    expect(screen.getByLabelText('Weight')).toHaveValue(75)
    expect(screen.getByLabelText('Height')).toHaveValue(6)
  })

  it('submits the edited values', async () => {
    const onSubmit = vi.fn()
    const user = userEvent.setup()
    render(<PokemonForm initialValues={initialValues} onSubmit={onSubmit} />)

    await user.clear(screen.getByLabelText('Region'))
    await user.type(screen.getByLabelText('Region'), 'Kanto')
    await user.click(screen.getByRole('button', { name: 'Save' }))

    expect(onSubmit).toHaveBeenCalledWith(
      expect.objectContaining({ name: 'clefairy', region: 'Kanto', weight: 75, height: 6 }),
    )
  })
})
