package io.secintel.platform.integration.github;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Verifies the {@code X-Hub-Signature-256} header: {@code sha256=} followed by the hex HMAC-SHA256 of the raw body.
 */
final class GitHubWebhookSignatureVerifier {

	private static final String ALGORITHM = "HmacSHA256";

	private static final String PREFIX = "sha256=";

	private static final int SIGNATURE_LENGTH = PREFIX.length() + 64;

	private final SecretKeySpec key;

	GitHubWebhookSignatureVerifier(String secret) {
		this.key = (secret == null || secret.isBlank()) ? null
				: new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
	}

	boolean isConfigured() {
		return key != null;
	}

	boolean isValid(byte[] body, String signatureHeader) {
		if (key == null || signatureHeader == null || signatureHeader.length() != SIGNATURE_LENGTH
				|| !signatureHeader.startsWith(PREFIX)) {
			return false;
		}
		byte[] provided;
		try {
			provided = HexFormat.of().parseHex(signatureHeader, PREFIX.length(), SIGNATURE_LENGTH);
		}
		catch (IllegalArgumentException ex) {
			return false;
		}
		return MessageDigest.isEqual(hmac(body), provided);
	}

	private byte[] hmac(byte[] body) {
		try {
			Mac mac = Mac.getInstance(ALGORITHM);
			mac.init(key);
			return mac.doFinal(body);
		}
		catch (GeneralSecurityException ex) {
			throw new IllegalStateException("HmacSHA256 is unavailable", ex);
		}
	}

}
