package io.secintel.platform.organization;

import java.time.Instant;
import java.util.UUID;

public record TeamSummary(UUID id, String name, Instant createdAt) {

	static TeamSummary from(Team team) {
		return new TeamSummary(team.getId(), team.getName(), team.getCreatedAt());
	}

}
