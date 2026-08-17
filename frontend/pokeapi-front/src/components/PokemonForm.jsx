import { useState } from 'react'

function PokemonForm({ initialValues, onSubmit, submitting }) {
  const [values, setValues] = useState({
    name: initialValues.name ?? '',
    weight: initialValues.weight ?? '',
    height: initialValues.height ?? '',
    localizedName: initialValues.localizedName ?? '',
    region: initialValues.region ?? '',
    tags: initialValues.tags ?? '',
  })

  function handleChange(field) {
    return (event) => setValues((prev) => ({ ...prev, [field]: event.target.value }))
  }

  function handleSubmit(event) {
    event.preventDefault()
    onSubmit({
      ...values,
      weight: values.weight === '' ? null : Number(values.weight),
      height: values.height === '' ? null : Number(values.height),
    })
  }

  return (
    <form onSubmit={handleSubmit}>
      <label>
        Name
        <input value={values.name} onChange={handleChange('name')} required />
      </label>
      <label>
        Weight
        <input type="number" value={values.weight} onChange={handleChange('weight')} />
      </label>
      <label>
        Height
        <input type="number" value={values.height} onChange={handleChange('height')} />
      </label>
      <label>
        Localized name
        <input value={values.localizedName} onChange={handleChange('localizedName')} />
      </label>
      <label>
        Region
        <input value={values.region} onChange={handleChange('region')} />
      </label>
      <label>
        Tags
        <input value={values.tags} onChange={handleChange('tags')} />
      </label>
      <button type="submit" disabled={submitting}>
        Save
      </button>
    </form>
  )
}

export default PokemonForm
