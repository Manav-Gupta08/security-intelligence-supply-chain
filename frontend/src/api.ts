export type Page<T> = {
  items: T[]
  page: {
    nextCursor: string | null
  }
}

export type Organization = {
  id: string
  slug: string
  displayName: string
  status: 'ACTIVE' | 'SUSPENDED'
  createdAt: string
}

export type Team = {
  id: string
  name: string
  createdAt: string
}

export class ApiError extends Error {
  readonly status: number
  readonly correlationId?: string

  constructor(message: string, status: number, correlationId?: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.correlationId = correlationId
  }
}

async function request<T>(path: string, signal?: AbortSignal): Promise<T> {
  const response = await fetch(path, {
    headers: { Accept: 'application/json' },
    signal,
  })

  if (!response.ok) {
    let message = `Request failed with status ${response.status}.`
    let correlationId = response.headers.get('X-Correlation-Id') ?? undefined

    try {
      const problem = (await response.json()) as {
        detail?: string
        correlationId?: string
      }
      message = problem.detail ?? message
      correlationId = problem.correlationId ?? correlationId
    } catch {
      // Keep the status-based message when the server response is not JSON.
    }

    throw new ApiError(message, response.status, correlationId)
  }

  return response.json() as Promise<T>
}

export function listOrganizations(signal?: AbortSignal) {
  return request<Page<Organization>>('/api/v1/organizations?limit=100', signal)
}

export function getOrganization(organizationId: string, signal?: AbortSignal) {
  return request<Organization>(`/api/v1/organizations/${organizationId}`, signal)
}

export function listTeams(organizationId: string, signal?: AbortSignal) {
  return request<Page<Team>>(`/api/v1/organizations/${organizationId}/teams?limit=100`, signal)
}
