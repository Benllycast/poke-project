function StatBar({ stat }) {
  const width = (Math.min(stat.baseStat, 150) / 150) * 100

  return (
    <div className="stat-bar">
      <span className="stat-bar-name">{stat.name}</span>
      <div className="stat-bar-track">
        <div className="stat-bar-fill" style={{ width: `${width}%` }} />
      </div>
      <span className="stat-bar-value">{stat.baseStat}</span>
    </div>
  )
}

export default StatBar
