import { Link } from 'react-router-dom'

function NotFoundPage() {
  return (
    <section>
      <h1>Page not found</h1>
      <p>
        <Link to="/">Back to the Pokedex</Link>
      </p>
    </section>
  )
}

export default NotFoundPage
