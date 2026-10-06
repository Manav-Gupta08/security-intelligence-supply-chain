package io.secintel.platform.integration.github;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import io.secintel.platform.common.ApiException;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * GitHub App webhook receiver. Authenticity is established by HMAC signature over the raw body before any parsing.
 */
@RestController
class GitHubWebhookController {

	private static final Pattern EVENT_TYPE = Pattern.compile("[a-z_]{1,64}");

	private static final Pattern DELIVERY_ID = Pattern.compile("[A-Za-z0-9-]{1,64}");

	private final GitHubWebhookSignatureVerifier verifier;

	private final GitHubWebhookDeliveryService deliveries;

	private final int maxPayloadBytes;

	GitHubWebhookController(GitHubWebhookSignatureVerifier verifier, GitHubWebhookDeliveryService deliveries,
			GitHubWebhookProperties properties) {
		this.verifier = verifier;
		this.deliveries = deliveries;
		this.maxPayloadBytes = properties.maxPayloadBytes();
	}

	@PostMapping(path = "/api/v1/webhooks/github", consumes = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<Void> receive(HttpServletRequest request,
			@RequestHeader(name = "X-Hub-Signature-256", required = false) String signature,
			@RequestHeader(name = "X-GitHub-Event", required = false) String eventType,
			@RequestHeader(name = "X-GitHub-Delivery", required = false) String deliveryId) throws IOException {
		if (!verifier.isConfigured()) {
			throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "The GitHub webhook receiver is not configured.");
		}
		byte[] body = readBoundedBody(request);
		if (!verifier.isValid(body, signature)) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "The webhook signature is missing or invalid.");
		}
		if (eventType == null || !EVENT_TYPE.matcher(eventType).matches() || deliveryId == null
				|| !DELIVERY_ID.matcher(deliveryId).matches()) {
			throw ApiException.badRequest("Required GitHub delivery headers are missing or invalid.");
		}
		return switch (deliveries.accept(eventType, deliveryId, body)) {
			case ACCEPTED -> ResponseEntity.accepted().build();
			case DUPLICATE -> ResponseEntity.ok().build();
		};
	}

	private byte[] readBoundedBody(HttpServletRequest request) throws IOException {
		if (request.getContentLengthLong() > maxPayloadBytes) {
			throw payloadTooLarge();
		}
		try (InputStream in = request.getInputStream()) {
			byte[] body = in.readNBytes(maxPayloadBytes + 1);
			if (body.length > maxPayloadBytes) {
				throw payloadTooLarge();
			}
			return body;
		}
	}

	private static ApiException payloadTooLarge() {
		return new ApiException(HttpStatus.CONTENT_TOO_LARGE, "The webhook payload exceeds the size limit.");
	}

}
