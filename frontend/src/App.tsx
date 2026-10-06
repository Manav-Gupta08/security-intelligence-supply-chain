import { useState } from 'react'

// Placeholder navigation matching the MVP dashboard views (FR-UI-002); no API calls yet.
const SECTIONS = [
  { id: 'overview', label: 'Overview' },
  { id: 'repositories', label: 'Repositories' },
  { id: 'posture', label: 'Security Posture' },
  { id: 'findings', label: 'Findings' },
  { id: 'vulnerabilities', label: 'Vulnerabilities' },
  { id: 'dependencies', label: 'Dependencies' },
  { id: 'sboms', label: 'SBOMs' },
  { id: 'events', label: 'Events' },
  { id: 'graph', label: 'Security Graph' },
  { id: 'integrations', label: 'Integrations' },
] as const

type SectionId = (typeof SECTIONS)[number]['id']

function App() {
  const [active, setActive] = useState<SectionId>('overview')
  const section = SECTIONS.find((s) => s.id === active) ?? SECTIONS[0]

  return (
    <div className="app">
      <header className="app-header">
        <strong>Security Intelligence</strong>
        <label>
          Organization{' '}
          <select disabled>
            <option>No organization</option>
          </select>
        </label>
      </header>
      <div className="app-body">
        <nav className="app-nav" aria-label="Main">
          <ul>
            {SECTIONS.map((s) => (
              <li key={s.id}>
                <button
                  type="button"
                  aria-current={s.id === active ? 'page' : undefined}
                  onClick={() => setActive(s.id)}
                >
                  {s.label}
                </button>
              </li>
            ))}
          </ul>
        </nav>
        <main className="app-main">
          <h1>{section.label}</h1>
          <p>Not implemented yet.</p>
        </main>
      </div>
    </div>
  )
}

export default App
