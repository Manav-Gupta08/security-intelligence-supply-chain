package io.secintel.platform.organization;

import java.util.UUID;

import io.secintel.platform.common.CursorPage;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Unauthenticated until the authentication decision (D-004) is implemented; exposes no personal data.
@RestController
@RequestMapping("/api/v1/organizations")
class OrganizationController {

	private final OrganizationQueryService queries;

	OrganizationController(OrganizationQueryService queries) {
		this.queries = queries;
	}

	@GetMapping
	CursorPage<OrganizationSummary> list(
			@RequestParam(defaultValue = CursorPage.DEFAULT_LIMIT) @Min(1) @Max(CursorPage.MAX_LIMIT) int limit,
			@RequestParam(required = false) String cursor) {
		return queries.listOrganizations(limit, cursor);
	}

	@GetMapping("/{organizationId}")
	OrganizationSummary get(@PathVariable UUID organizationId) {
		return queries.getOrganization(organizationId);
	}

	@GetMapping("/{organizationId}/teams")
	CursorPage<TeamSummary> teams(@PathVariable UUID organizationId,
			@RequestParam(defaultValue = CursorPage.DEFAULT_LIMIT) @Min(1) @Max(CursorPage.MAX_LIMIT) int limit,
			@RequestParam(required = false) String cursor) {
		return queries.listTeams(organizationId, limit, cursor);
	}

}
