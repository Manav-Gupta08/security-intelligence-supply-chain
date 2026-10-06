package io.secintel.platform.organization;

import java.time.Instant;
import java.util.UUID;

public record OrganizationSummary(UUID id, String slug, String displayName, String status, Instant createdAt) {

	static OrganizationSummary from(Organization organization) {
		return new OrganizationSummary(organization.getId(), organization.getSlug(), organization.getDisplayName(),
				organization.getStatus().name(), organization.getCreatedAt());
	}

}
