package io.secintel.platform.integration.github;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import io.secintel.platform.TestcontainersConfiguration;
import io.secintel.platform.common.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class GitHubWebhookDeliveryServiceIntegrationTests {

	private static final long INSTALLATION_ID = 4242L;

	@Autowired
	private GitHubWebhookDeliveryService service;

	@Autowired
	private JdbcClient jdbc;

	private UUID organizationId;

	@BeforeEach
	void bindInstallation() {
		organizationId = UUID.randomUUID();
		jdbc.sql("insert into organization (id, slug, display_name, status) values (:id, :slug, 'Acme', 'ACTIVE')")
			.param("id", organizationId)
			.param("slug", "acme-" + organizationId.toString().substring(0, 8))
			.update();
		jdbc.sql("""
				insert into integration_installation (id, organization_id, provider, provider_installation_id,
				    provider_account_id, provider_account_login, status)
				values (:id, :organizationId, 'GITHUB', :installationId, 1, 'acme', 'ACTIVE')
				""")
			.param("id", UUID.randomUUID())
			.param("organizationId", organizationId)
			.param("installationId", INSTALLATION_ID)
			.update();
	}

	@Test
	void recordsDeliveryOnceAndAttributesItToTheInstallationOrganization() {
		String deliveryId = UUID.randomUUID().toString();
		byte[] payload = payload("{\"action\":\"opened\",\"installation\":{\"id\":" + INSTALLATION_ID + "}}");

		assertThat(service.accept("issues", deliveryId, payload)).isEqualTo(GitHubWebhookDeliveryService.Outcome.ACCEPTED);
		assertThat(service.accept("issues", deliveryId, payload))
			.isEqualTo(GitHubWebhookDeliveryService.Outcome.DUPLICATE);

		Map<String, Object> row = delivery(deliveryId);
		assertThat(row).containsEntry("organization_id", organizationId)
			.containsEntry("processing_status", "RECEIVED")
			.containsEntry("event_type", "issues")
			.containsEntry("event_action", "opened")
			.containsEntry("provider_installation_id", INSTALLATION_ID);
		assertThat((String) row.get("payload_sha256")).hasSize(64);
		assertThat(jdbc.sql("select count(*) from webhook_delivery where provider_delivery_id = :id")
			.param("id", deliveryId)
			.query(Long.class)
			.single()).isEqualTo(1L);
	}

	@Test
	void recordsUnknownInstallationAsUnbound() {
		String deliveryId = UUID.randomUUID().toString();

		service.accept("installation", deliveryId, payload("{\"action\":\"created\",\"installation\":{\"id\":999}}"));

		assertThat(delivery(deliveryId)).containsEntry("organization_id", null)
			.containsEntry("processing_status", "UNBOUND");
	}

	@Test
	void rejectsMalformedPayloadsWithoutRecording() {
		String deliveryId = UUID.randomUUID().toString();

		for (String body : new String[] { "not json", "[]", "{\"action\":\"DROP TABLE\"}",
				"{\"installation\":{\"id\":-1}}" }) {
			assertThatExceptionOfType(ApiException.class)
				.isThrownBy(() -> service.accept("issues", deliveryId, payload(body)));
		}
		assertThat(jdbc.sql("select count(*) from webhook_delivery where provider_delivery_id = :id")
			.param("id", deliveryId)
			.query(Long.class)
			.single()).isZero();
	}

	private Map<String, Object> delivery(String deliveryId) {
		return jdbc.sql("select * from webhook_delivery where provider_delivery_id = :id")
			.param("id", deliveryId)
			.query()
			.singleRow();
	}

	private static byte[] payload(String json) {
		return json.getBytes(StandardCharsets.UTF_8);
	}

}
