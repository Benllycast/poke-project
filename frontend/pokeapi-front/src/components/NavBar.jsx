import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

function NavBar() {
  const { isAuthenticated, email, logout } = useAuth()

  return (
    <nav className="nav-bar">
      <Link to="/" className="nav-brand">
        Pokedex
      </Link>
      {isAuthenticated ? (
        <span>
          {email}{' '}
          <button type="button" onClick={logout}>
            Log out
          </button>
        </span>
      ) : (
        <span>
          <Link to="/login">Log in</Link> <Link to="/register">Register</Link>
        </span>
      )}
    </nav>
  )
}

export default NavBar
