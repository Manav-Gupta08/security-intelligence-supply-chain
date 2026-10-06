package io.secintel.platform.organization;

import java.util.List;
import java.util.UUID;

import io.secintel.platform.common.ApiException;
import io.secintel.platform.common.CursorPage;

import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrganizationQueryService {

	private static final String ORGANIZATION_SCOPE = "organizations:slug";

	private static final String TEAM_SCOPE = "teams:name";

	private final OrganizationRepository organizations;

	private final TeamRepository teams;

	OrganizationQueryService(OrganizationRepository organizations, TeamRepository teams) {
		this.organizations = organizations;
		this.teams = teams;
	}

	public CursorPage<OrganizationSummary> listOrganizations(int limit, String cursor) {
		String afterSlug = CursorPage.decodeCursor(ORGANIZATION_SCOPE, cursor);
		Limit probe = Limit.of(limit + 1);
		List<Organization> fetched = (afterSlug == null) ? organizations.findAllByOrderBySlugAsc(probe)
				: organizations.findBySlugGreaterThanOrderBySlugAsc(afterSlug, probe);
		return CursorPage.of(fetched, limit, ORGANIZATION_SCOPE, Organization::getSlug).map(OrganizationSummary::from);
	}

	public OrganizationSummary getOrganization(UUID organizationId) {
		return organizations.findById(organizationId)
			.map(OrganizationSummary::from)
			.orElseThrow(ApiException::notFound);
	}

	public CursorPage<TeamSummary> listTeams(UUID organizationId, int limit, String cursor) {
		String afterName = CursorPage.decodeCursor(TEAM_SCOPE, cursor);
		if (!organizations.existsById(organizationId)) {
			throw ApiException.notFound();
		}
		Limit probe = Limit.of(limit + 1);
		List<Team> fetched = (afterName == null) ? teams.findByOrganizationIdOrderByNameAsc(organizationId, probe)
				: teams.findByOrganizationIdAndNameGreaterThanOrderByNameAsc(organizationId, afterName, probe);
		return CursorPage.of(fetched, limit, TEAM_SCOPE, Team::getName).map(TeamSummary::from);
	}

}
