package io.secintel.platform.organization;

import java.util.List;
import java.util.UUID;

import io.secintel.platform.TestcontainersConfiguration;
import io.secintel.platform.common.ApiException;
import io.secintel.platform.common.CursorPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class OrganizationQueryServiceIntegrationTests {

	@Autowired
	private OrganizationQueryService queries;

	@Autowired
	private JdbcClient jdbc;

	private UUID organizationId;

	@BeforeEach
	void seed() {
		organizationId = UUID.randomUUID();
		jdbc.sql("insert into organization (id, slug, display_name, status) values (:id, 'zz-test-org', 'Test', 'ACTIVE')")
			.param("id", organizationId)
			.update();
		for (String name : List.of("platform", "appsec", "payments")) {
			jdbc.sql("insert into team (id, organization_id, name) values (:id, :organizationId, :name)")
				.param("id", UUID.randomUUID())
				.param("organizationId", organizationId)
				.param("name", name)
				.update();
		}
	}

	@Test
	void pagesTeamsInStableNameOrder() {
		CursorPage<TeamSummary> first = queries.listTeams(organizationId, 2, null);
		CursorPage<TeamSummary> second = queries.listTeams(organizationId, 2, first.page().nextCursor());

		assertThat(first.items()).extracting(TeamSummary::name).containsExactly("appsec", "payments");
		assertThat(second.items()).extracting(TeamSummary::name).containsExactly("platform");
		assertThat(second.page().nextCursor()).isNull();
	}

	@Test
	void teamCursorCannotBeReplayedAgainstOrganizations() {
		String teamCursor = queries.listTeams(organizationId, 1, null).page().nextCursor();

		assertThatExceptionOfType(ApiException.class).isThrownBy(() -> queries.listOrganizations(10, teamCursor));
	}

	@Test
	void unknownOrganizationIsNotFound() {
		assertThatExceptionOfType(ApiException.class).isThrownBy(() -> queries.getOrganization(UUID.randomUUID()));
		assertThatExceptionOfType(ApiException.class)
			.isThrownBy(() -> queries.listTeams(UUID.randomUUID(), 10, null));
	}

	@Test
	void databaseRejectsTeamMemberFromAnotherOrganization() {
		UUID otherOrganization = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		jdbc.sql("insert into organization (id, slug, display_name, status) values (:id, 'zz-other-org', 'Other', 'ACTIVE')")
			.param("id", otherOrganization)
			.update();
		jdbc.sql("insert into user_account (id, email, display_name, status) values (:id, 'dev@example.com', 'Dev', 'ACTIVE')")
			.param("id", userId)
			.update();
		jdbc.sql("insert into membership (id, organization_id, user_id, role) values (:id, :org, :user, 'DEVELOPER')")
			.param("id", UUID.randomUUID())
			.param("org", otherOrganization)
			.param("user", userId)
			.update();
		UUID teamId = jdbc.sql("select id from team where organization_id = :org and name = 'appsec'")
			.param("org", organizationId)
			.query(UUID.class)
			.single();

		assertThatExceptionOfType(DataIntegrityViolationException.class)
			.isThrownBy(() -> jdbc.sql("insert into team_membership (organization_id, team_id, user_id) values (:org, :team, :user)")
				.param("org", organizationId)
				.param("team", teamId)
				.param("user", userId)
				.update());
	}

}
