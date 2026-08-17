import { Link } from 'react-router-dom'

function EvolutionChain({ stages }) {
  if (!stages || stages.length === 0) {
    return null
  }

  return (
    <ol className="evolution-chain">
      {stages.map((stage) => (
        <li key={stage.speciesId}>
          <Link to={`/pokemon/${stage.speciesId}`}>{stage.name}</Link>
          {stage.minLevel != null && <span> (Lv. {stage.minLevel})</span>}
        </li>
      ))}
    </ol>
  )
}

export default EvolutionChain
