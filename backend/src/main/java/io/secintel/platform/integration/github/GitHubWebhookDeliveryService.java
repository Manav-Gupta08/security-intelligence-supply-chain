package io.secintel.platform.integration.github;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import java.util.regex.Pattern;

import io.secintel.platform.common.ApiException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Records signature-verified GitHub deliveries exactly once, keyed by the GitHub delivery ID.
 */
@Service
class GitHubWebhookDeliveryService {

	enum Outcome {

		ACCEPTED, DUPLICATE

	}

	private static final Pattern ACTION = Pattern.compile("[a-z_]{1,64}");

	private final JdbcClient jdbc;

	private final ObjectMapper objectMapper;

	GitHubWebhookDeliveryService(JdbcClient jdbc, ObjectMapper objectMapper) {
		this.jdbc = jdbc;
		this.objectMapper = objectMapper;
	}

	/**
	 * @param payload raw body whose signature has already been verified
	 */
	@Transactional
	Outcome accept(String eventType, String deliveryId, byte[] payload) {
		Envelope envelope = parse(payload);
		Long installationId = (envelope.installation() != null) ? envelope.installation().id() : null;
		UUID organizationId = (installationId != null) ? findActiveOrganization(installationId) : null;

		int inserted = jdbc.sql("""
				insert into webhook_delivery (id, organization_id, provider, provider_delivery_id,
				    provider_installation_id, event_type, event_action, payload_sha256, processing_status)
				values (:id, :organizationId, 'GITHUB', :deliveryId, :installationId, :eventType, :action, :digest, :status)
				on conflict (provider, provider_delivery_id) do nothing
				""")
			.param("id", UUID.randomUUID())
			.param("organizationId", organizationId)
			.param("deliveryId", deliveryId)
			.param("installationId", installationId)
			.param("eventType", eventType)
			.param("action", envelope.action())
			.param("digest", sha256Hex(payload))
			.param("status", (organizationId != null) ? "RECEIVED" : "UNBOUND")
			.update();
		return (inserted == 1) ? Outcome.ACCEPTED : Outcome.DUPLICATE;
	}

	private Envelope parse(byte[] payload) {
		Envelope envelope;
		try {
			envelope = objectMapper.readValue(payload, Envelope.class);
		}
		catch (JacksonException ex) {
			throw invalidPayload();
		}
		if (envelope == null || (envelope.action() != null && !ACTION.matcher(envelope.action()).matches())
				|| (envelope.installation() != null
						&& (envelope.installation().id() == null || envelope.installation().id() <= 0))) {
			throw invalidPayload();
		}
		return envelope;
	}

	private UUID findActiveOrganization(long installationId) {
		return jdbc.sql("""
				select organization_id from integration_installation
				where provider = 'GITHUB' and provider_installation_id = :installationId and status = 'ACTIVE'
				""")
			.param("installationId", installationId)
			.query(UUID.class)
			.optional()
			.orElse(null);
	}

	private static String sha256Hex(byte[] payload) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(payload));
		}
		catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 is unavailable", ex);
		}
	}

	private static ApiException invalidPayload() {
		return ApiException.badRequest("The webhook payload is not a valid GitHub event.");
	}

	/** The only payload fields read before normalization; everything else is ignored. */
	record Envelope(String action, Installation installation) {

		record Installation(Long id) {
		}

	}

}
