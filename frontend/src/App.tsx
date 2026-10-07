import { NavLink, Navigate, Route, Routes, useLocation } from 'react-router-dom'
import { useCallback, useEffect, useState } from 'react'
import { AnimatePresence, motion } from 'motion/react'
import type { ReactNode } from 'react'
import './App.css'
import { ApiError, getOrganization, listOrganizations, listTeams, type Organization, type Team } from './api'

type PageDefinition = {
  label: string
  path: string
  icon: string
}

const pages: PageDefinition[] = [
  { label: 'Overview', path: '/', icon: '◈' },
  { label: 'Onboarding', path: '/onboarding', icon: '+' },
  { label: 'Repositories', path: '/repositories', icon: '▣' },
  { label: 'Integrations', path: '/integrations', icon: '↔' },
  { label: 'Jobs', path: '/jobs', icon: '◷' },
  { label: 'Findings', path: '/findings', icon: '!' },
  { label: 'Vulnerabilities', path: '/vulnerabilities', icon: '◇' },
  { label: 'Dependencies', path: '/dependencies', icon: '⌘' },
  { label: 'SBOMs', path: '/sboms', icon: '▤' },
  { label: 'Posture', path: '/posture', icon: '◒' },
  { label: 'Events', path: '/events', icon: '◷' },
  { label: 'Relationships', path: '/relationships', icon: '⊙' },
  { label: 'Administration', path: '/administration', icon: '⚙' },
]

type ScreenDefinition = {
  eyebrow: string
  title: string
  description: string
  mode?: 'list' | 'detail' | 'auth' | 'onboarding'
  tabs?: string[]
  columns?: string[]
}

const pageCopy: Record<string, ScreenDefinition> = {
  '/onboarding': {
    eyebrow: 'Organization onboarding',
    title: 'Connect your security workspace',
    description: 'Select an organization, connect GitHub, and monitor the initial synchronization.',
    mode: 'onboarding',
  },
  '/repositories': {
    eyebrow: 'Repository inventory',
    title: 'Repository inventory',
    description: 'Review connected repositories and their current security posture.',
    mode: 'list',
    columns: ['Repository', 'Ownership', 'Visibility', 'Ecosystem', 'Last sync'],
  },
  '/repositories/example': {
    eyebrow: 'Repository details',
    title: 'Repository details',
    description: 'Review posture, dependencies, findings, SBOMs, and events for a repository.',
    mode: 'detail',
    tabs: ['Summary', 'Posture', 'Dependencies', 'Findings', 'SBOMs', 'Events'],
  },
  '/integrations': {
    eyebrow: 'GitHub integrations',
    title: 'GitHub integrations',
    description: 'Manage installation status, capabilities, synchronization, and disconnect workflows.',
    mode: 'list',
    columns: ['Integration', 'Status', 'Capabilities', 'Last sync', 'Actions'],
  },
  '/jobs': {
    eyebrow: 'Scan and job status',
    title: 'Operations and synchronization',
    description: 'Start supported operations and track progress, errors, cancellation, and completion.',
    mode: 'list',
    columns: ['Operation', 'Scope', 'Status', 'Started', 'Progress'],
  },
  '/findings': {
    eyebrow: 'Security findings',
    title: 'Security findings',
    description: 'Investigate open risks, their evidence, and recommended remediation.',
    mode: 'list',
    columns: ['Finding', 'Severity', 'Status', 'Repository', 'Type'],
  },
  '/findings/example': {
    eyebrow: 'Finding details',
    title: 'Finding details',
    description: 'Review evidence, affected assets, dependency paths, remediation, and status transitions.',
    mode: 'detail',
    tabs: ['Evidence', 'Affected assets', 'Dependency paths', 'Remediation', 'History'],
  },
  '/vulnerabilities': {
    eyebrow: 'Vulnerability intelligence',
    title: 'Vulnerabilities',
    description: 'Track advisories affecting packages observed in your organization.',
    mode: 'list',
    columns: ['Advisory', 'Severity', 'Affected packages', 'Repositories', 'Published'],
  },
  '/vulnerabilities/example': {
    eyebrow: 'Vulnerability details',
    title: 'Vulnerability details',
    description: 'Review advisory sources, aliases, affected and fixed versions, and impact.',
    mode: 'detail',
    tabs: ['Overview', 'Affected versions', 'Fixed versions', 'Sources', 'Impact'],
  },
  '/dependencies': {
    eyebrow: 'Dependency intelligence',
    title: 'Dependencies',
    description: 'Explore packages, versions, direct/transitive status, and affected repositories.',
    mode: 'list',
    columns: ['Package', 'Ecosystem', 'Version', 'Relationship', 'Repositories'],
  },
  '/dependencies/example': {
    eyebrow: 'Dependency details',
    title: 'Dependency details',
    description: 'Trace dependency paths, related vulnerabilities, and repository relationships.',
    mode: 'detail',
    tabs: ['Dependency paths', 'Vulnerabilities', 'Repositories'],
  },
  '/sboms': {
    eyebrow: 'SBOM management',
    title: 'SBOM documents',
    description: 'Import CycloneDX or SPDX documents, validate them, and monitor processing status.',
    mode: 'list',
    columns: ['Document', 'Repository', 'Format', 'Status', 'Updated'],
  },
  '/sboms/import': {
    eyebrow: 'SBOM import',
    title: 'Import an SBOM',
    description: 'Select a repository, upload a CycloneDX or SPDX document, and validate processing.',
    mode: 'onboarding',
  },
  '/sboms/example': {
    eyebrow: 'SBOM details',
    title: 'SBOM details',
    description: 'Review document metadata, normalized components, and related vulnerabilities.',
    mode: 'detail',
    tabs: ['Metadata', 'Components', 'Vulnerabilities'],
  },
  '/posture': {
    eyebrow: 'Security posture',
    title: 'Security posture',
    description: 'Monitor control evaluations across your repositories.',
    mode: 'list',
    columns: ['Control', 'Result', 'Repositories', 'Rule version', 'Evaluated'],
  },
  '/posture/example': {
    eyebrow: 'Posture evaluation details',
    title: 'Posture evaluation details',
    description: 'Review evidence, rule version, evaluation time, history, and remediation guidance.',
    mode: 'detail',
    tabs: ['Evidence', 'History', 'Remediation'],
  },
  '/events': {
    eyebrow: 'Audit timeline',
    title: 'Security events',
    description: 'Follow security-relevant activity from integrations and users.',
    mode: 'list',
    columns: ['Event', 'Actor', 'Action', 'Resource', 'Time'],
  },
  '/events/example': {
    eyebrow: 'Event details',
    title: 'Event details',
    description: 'Review provenance, before/after changes, and related assets.',
    mode: 'detail',
    tabs: ['Provenance', 'Changes', 'Related assets'],
  },
  '/relationships': {
    eyebrow: 'Security relationships',
    title: 'Security relationships',
    description: 'Bounded exploration of repositories, packages, vulnerabilities, findings, and events.',
    mode: 'detail',
    tabs: ['Repositories', 'Packages', 'Vulnerabilities', 'Findings', 'Events'],
  },
  '/administration': {
    eyebrow: 'Organization administration',
    title: 'Organization administration',
    description: 'Manage organization information, membership, roles, and teams when mutation APIs are finalized.',
    mode: 'list',
    columns: ['Member or team', 'Role', 'Status', 'Created', 'Actions'],
  },
  '/login': {
    eyebrow: 'Authentication',
    title: 'Sign in to SecIntel',
    description: 'Authentication method is pending the organization identity decision.',
    mode: 'auth',
  },
  '/session/callback': {
    eyebrow: 'Authentication callback',
    title: 'Completing sign-in',
    description: 'The identity callback will establish a session when the authentication method is finalized.',
    mode: 'auth',
  },
  '/session/expired': {
    eyebrow: 'Session expired',
    title: 'Your session has expired',
    description: 'Sign in again to continue. Session renewal is pending the authentication API decision.',
    mode: 'auth',
  },
}

function App() {
  const [organizations, setOrganizations] = useState<Organization[]>([])
  const [selectedOrganizationId, setSelectedOrganizationId] = useState('')
  const [teams, setTeams] = useState<Team[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const loadOrganizations = useCallback(async (signal?: AbortSignal) => {
    setIsLoading(true)
    setError(null)
    try {
      const result = await listOrganizations(signal)
      setOrganizations(result.items)
      setSelectedOrganizationId((current) => (
        result.items.some((organization) => organization.id === current)
          ? current
          : result.items[0]?.id ?? ''
      ))
    } catch (cause) {
      if (cause instanceof DOMException && cause.name === 'AbortError') return
      setError(getErrorMessage(cause))
    } finally {
      if (!signal?.aborted) setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    const controller = new AbortController()
    void Promise.resolve().then(() => loadOrganizations(controller.signal))
    return () => controller.abort()
  }, [loadOrganizations])

  useEffect(() => {
    if (!selectedOrganizationId) {
      return
    }
    const controller = new AbortController()
    void Promise.all([
      getOrganization(selectedOrganizationId, controller.signal),
      listTeams(selectedOrganizationId, controller.signal),
    ]).then(([, teamPage]) => {
      setTeams(teamPage.items)
    }).catch((cause) => {
      if (!(cause instanceof DOMException && cause.name === 'AbortError')) {
        setError(getErrorMessage(cause))
      }
    })
    return () => controller.abort()
  }, [selectedOrganizationId])

  const selectedOrganization = organizations.find(({ id }) => id === selectedOrganizationId)

  return (
    <div className="app-shell">
      <Sidebar />
      <div className="app-content">
        <Header
          organizations={organizations}
          selectedOrganizationId={selectedOrganizationId}
          onOrganizationChange={setSelectedOrganizationId}
          isLoading={isLoading}
        />
        <motion.main
          className="main-content"
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.25, ease: 'easeOut' }}
        >
          {error && (
            <motion.div
              className="api-alert flex-wrap"
              role="alert"
              initial={{ opacity: 0, y: -6 }}
              animate={{ opacity: 1, y: 0 }}
            >
              <strong>Unable to load workspace data.</strong>
              <span>{error}</span>
              <button type="button" onClick={() => void loadOrganizations()}>Retry</button>
            </motion.div>
          )}
          <Routes>
            <Route path="/" element={<PageTransition><Overview organization={selectedOrganization} teams={teams} isLoading={isLoading} /></PageTransition>} />
            {Object.entries(pageCopy).map(([path, copy]) => (
              <Route key={path} path={path} element={<PageTransition><WorkspaceScreen {...copy} /></PageTransition>} />
            ))}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </motion.main>
      </div>
    </div>
  )
}

function PageTransition({ children }: { children: ReactNode }) {
  const location = useLocation()

  return (
    <AnimatePresence mode="wait">
      <motion.div
        key={location.pathname}
        initial={{ opacity: 0, x: 8 }}
        animate={{ opacity: 1, x: 0 }}
        exit={{ opacity: 0, x: -8 }}
        transition={{ duration: 0.18, ease: 'easeOut' }}
      >
        {children}
      </motion.div>
    </AnimatePresence>
  )
}

function Sidebar() {
  return (
    <aside className="sidebar">
      <div className="brand">
        <span className="brand-mark">S</span>
        <span>
          <strong>SecIntel</strong>
          <small>Security intelligence</small>
        </span>
      </div>

      <nav aria-label="Primary navigation">
        <span className="nav-heading">Workspace</span>
        {pages.map((page) => (
          <NavLink
            key={page.path}
            to={page.path}
            end={page.path === '/'}
            className={({ isActive }: { isActive: boolean }) => (isActive ? 'nav-link active' : 'nav-link')}
          >
            <span className="nav-icon" aria-hidden="true">{page.icon}</span>
            {page.label}
          </NavLink>
        ))}
      </nav>

      <div className="sidebar-footer">
        <span className="status-dot" aria-hidden="true" />
        <span>
          <strong>All systems normal</strong>
          <small>Last checked just now</small>
        </span>
      </div>
    </aside>
  )
}

function Header({
  organizations,
  selectedOrganizationId,
  onOrganizationChange,
  isLoading,
}: {
  organizations: Organization[]
  selectedOrganizationId: string
  onOrganizationChange: (id: string) => void
  isLoading: boolean
}) {
  const location = useLocation()
  const currentPage = pages.find((page) => page.path === location.pathname)
    ?? Object.entries(pageCopy).sort(([a], [b]) => b.length - a.length).find(([path]) => location.pathname.startsWith(path))?.[1]
  const pageTitle = currentPage && 'label' in currentPage ? currentPage.label : currentPage?.title ?? 'Overview'

  return (
    <header className="topbar">
      <div>
        <p className="breadcrumb">Security workspace <span>/</span> {pageTitle}</p>
        <h1>{pageTitle}</h1>
      </div>
      <div className="topbar-actions">
        <label className="organization-selector">
          <span>Organization</span>
          <select
            value={selectedOrganizationId}
            onChange={(event) => onOrganizationChange(event.target.value)}
            disabled={isLoading || organizations.length === 0}
            aria-label="Select organization"
          >
            {organizations.length === 0 && <option value="">No organizations</option>}
            {organizations.map((organization) => (
              <option key={organization.id} value={organization.id}>{organization.displayName}</option>
            ))}
          </select>
        </label>
        <button className="avatar" type="button" aria-label="Open profile menu">MG</button>
      </div>
    </header>
  )
}

function Overview({
  organization,
  teams,
  isLoading,
}: {
  organization?: Organization
  teams: Team[]
  isLoading: boolean
}) {
  if (isLoading) {
    return <section className="loading-state" aria-live="polite">Loading workspace data…</section>
  }

  if (!organization) {
    return (
      <section className="empty-state">
        <div className="empty-icon" aria-hidden="true">◌</div>
        <p className="eyebrow">Organization overview</p>
        <h2>No organization connected</h2>
        <p className="muted">The API returned no organizations for this workspace.</p>
      </section>
    )
  }

  return (
    <>
      <section className="page-intro">
        <div>
          <p className="eyebrow">Organization overview</p>
          <h2>{organization.displayName}</h2>
          <p className="muted">Connected organization <strong>{organization.slug}</strong> · {organization.status.toLowerCase()}</p>
        </div>
        <span className="connection-badge"><span className="status-dot" aria-hidden="true" /> API connected</span>
      </section>

      <section className="metric-grid" aria-label="Security summary">
        <MetricCard label="Teams" value={String(teams.length)} detail="Loaded from the organization API" tone="blue" />
        <MetricCard label="Repositories" value="—" detail="Available in Phase 3" tone="orange" />
        <MetricCard label="Open findings" value="—" detail="Available in Phase 4" tone="red" />
        <MetricCard label="Posture score" value="—" detail="Available in Phase 4" tone="green" />
      </section>

      <section className="dashboard-grid">
        <div className="panel">
          <div className="panel-header">
            <div>
              <p className="eyebrow">Organization data</p>
              <h3>Teams</h3>
            </div>
            <span className="panel-count">{teams.length}</span>
          </div>
          <div className="finding-list">
            {teams.length > 0 ? teams.map((team) => (
              <div className="finding-row" key={team.id}>
                <span className="team-icon" aria-hidden="true">#</span>
                <div><strong>{team.name}</strong><span>Team in {organization.displayName}</span></div>
              </div>
            )) : <p className="panel-empty">No teams have been created yet.</p>}
          </div>
        </div>
        <div className="panel">
          <div className="panel-header">
            <div><p className="eyebrow">Organization status</p><h3>Connection details</h3></div>
          </div>
          <div className="event-list">
            <Event text="Organization loaded" time={formatDate(organization.createdAt)} />
            <Event text="Teams endpoint available" time={`${teams.length} returned`} />
            <Event text="Authentication" time="Not enabled in Phase 1" />
          </div>
        </div>
      </section>
    </>
  )
}

function MetricCard({ label, value, detail, tone }: { label: string; value: string; detail: string; tone: string }) {
  return (
    <article className={`metric-card ${tone}`}>
      <span className="metric-label">{label}</span>
      <strong>{value}</strong>
      <span className="metric-detail">{detail}</span>
    </article>
  )
}

function Event({ text, time }: { text: string; time: string }) {
  return (
    <div className="event-row">
      <span className="event-marker" aria-hidden="true" />
      <div>
        <strong>{text}</strong>
        <span>{time}</span>
      </div>
    </div>
  )
}

function WorkspaceScreen({ eyebrow, title, description, mode = 'list', tabs, columns }: ScreenDefinition) {
  if (mode === 'auth') {
    return (
      <section className="empty-state auth-state">
        <div className="empty-icon" aria-hidden="true">S</div>
        <p className="eyebrow">{eyebrow}</p>
        <h2>{title}</h2>
        <p className="muted">{description}</p>
        <button className="secondary-button" type="button" disabled>Authentication decision pending</button>
        <span className="coming-soon">Expired-session handling will be enabled with the auth API</span>
      </section>
    )
  }

  if (mode === 'onboarding') {
    return (
      <section>
        <section className="page-intro">
          <div><p className="eyebrow">{eyebrow}</p><h2>{title}</h2><p className="muted">{description}</p></div>
          <span className="connection-badge">Phase 1 foundation</span>
        </section>
        <div className="onboarding-grid">
          <article className="onboarding-card"><span className="step-number">1</span><h3>Select organization</h3><p>Choose the organization from the API-connected selector in the header.</p><span className="coming-soon">Available now</span></article>
          <article className="onboarding-card"><span className="step-number">2</span><h3>Connect GitHub</h3><p>Install the GitHub App and grant repository capabilities.</p><span className="coming-soon">Phase 3 integration</span></article>
          <article className="onboarding-card"><span className="step-number">3</span><h3>Initial synchronization</h3><p>Track repository discovery, scan progress, errors, and completion.</p><span className="coming-soon">Phase 3 integration</span></article>
        </div>
      </section>
    )
  }

  if (mode === 'detail') {
    return (
      <section>
        <section className="page-intro">
          <div><p className="eyebrow">{eyebrow}</p><h2>{title}</h2><p className="muted">{description}</p></div>
          <span className="coming-soon">Data unavailable in Phase 1</span>
        </section>
        <div className="detail-summary"><div><span className="metric-label">Status</span><strong>Not evaluated</strong></div><div><span className="metric-label">Last updated</span><strong>—</strong></div><div><span className="metric-label">Related assets</span><strong>—</strong></div></div>
        <div className="tab-list" role="tablist">{(tabs ?? ['Overview']).map((tab, index) => <button key={tab} className={index === 0 ? 'tab active' : 'tab'} type="button">{tab}</button>)}</div>
        <div className="panel detail-panel"><div className="empty-icon" aria-hidden="true">◌</div><h3>Details will appear here</h3><p className="muted">This screen is wired for the Phase 1 navigation shell. Its data contract is owned by a later phase.</p></div>
      </section>
    )
  }

  return (
    <section>
      <section className="page-intro">
        <div><p className="eyebrow">{eyebrow}</p><h2>{title}</h2><p className="muted">{description}</p></div>
        <button className="secondary-button" type="button" disabled>Data unavailable in Phase 1</button>
      </section>
      <div className="filter-bar"><input aria-label="Search" placeholder="Search..." disabled /><select aria-label="Filter" disabled><option>All statuses</option></select><span className="coming-soon">Filters activate with the API</span></div>
      <div className="panel data-table"><div className="table-header">{(columns ?? ['Name', 'Status', 'Updated']).map((column) => <strong key={column}>{column}</strong>)}</div><div className="table-empty"><div className="empty-icon" aria-hidden="true">◌</div><h3>No data available</h3><p className="muted">This inventory is ready for its backend contract and will remain empty until that phase is implemented.</p></div></div>
    </section>
  )
}

function getErrorMessage(cause: unknown) {
  if (cause instanceof ApiError) {
    return cause.correlationId ? `${cause.message} (Reference: ${cause.correlationId})` : cause.message
  }
  return 'Check that the backend is running and try again.'
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(new Date(value))
}

export default App
