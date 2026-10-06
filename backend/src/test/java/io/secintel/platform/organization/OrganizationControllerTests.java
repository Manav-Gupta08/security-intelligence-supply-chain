package io.secintel.platform.organization;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import io.secintel.platform.common.ApiException;
import io.secintel.platform.common.CursorPage;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrganizationController.class)
class OrganizationControllerTests {

	private static final UUID ORGANIZATION_ID = UUID.fromString("0b8f2f4e-8c39-4c43-a8f4-4f0c8f8b5d0a");

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private OrganizationQueryService queries;

	@Test
	void listsOrganizationsWithDefaultLimit() throws Exception {
		OrganizationSummary acme = new OrganizationSummary(ORGANIZATION_ID, "acme", "Acme", "ACTIVE",
				Instant.parse("2026-10-06T10:00:00Z"));
		given(queries.listOrganizations(50, null))
			.willReturn(new CursorPage<>(List.of(acme), new CursorPage.PageInfo("next")));

		mvc.perform(get("/api/v1/organizations"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].slug").value("acme"))
			.andExpect(jsonPath("$.items[0].createdAt").value("2026-10-06T10:00:00Z"))
			.andExpect(jsonPath("$.page.nextCursor").value("next"));
	}

	@Test
	void rejectsLimitAboveMaximum() throws Exception {
		mvc.perform(get("/api/v1/organizations").param("limit", "101"))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));

		verifyNoInteractions(queries);
	}

	@Test
	void rejectsMalformedOrganizationId() throws Exception {
		mvc.perform(get("/api/v1/organizations/not-a-uuid")).andExpect(status().isBadRequest());

		verifyNoInteractions(queries);
	}

	@Test
	void unknownOrganizationIsProblemWithoutInternals() throws Exception {
		given(queries.getOrganization(ORGANIZATION_ID)).willThrow(ApiException.notFound());

		mvc.perform(get("/api/v1/organizations/{id}", ORGANIZATION_ID))
			.andExpect(status().isNotFound())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.status").value(404))
			.andExpect(jsonPath("$.correlationId").isNotEmpty())
			.andExpect(jsonPath("$.trace").doesNotExist())
			.andExpect(jsonPath("$.exception").doesNotExist());
	}

	@Test
	void unexpectedFailureIsGenericProblem() throws Exception {
		given(queries.listTeams(any(), anyInt(), any())).willThrow(new IllegalStateException("db password=secret"));

		mvc.perform(get("/api/v1/organizations/{id}/teams", ORGANIZATION_ID))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.detail").value("An unexpected error occurred."))
			.andExpect(content().string(not(containsString("secret"))));
	}

}
