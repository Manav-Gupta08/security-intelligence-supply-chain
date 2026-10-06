package io.secintel.platform.integration.github;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GitHubWebhookSignatureVerifierTests {

	// Test vector published in GitHub's "Validating webhook deliveries" documentation.
	private static final String SECRET = "It's a Secret to Everybody";

	private static final byte[] BODY = "Hello, World!".getBytes(StandardCharsets.UTF_8);

	private static final String SIGNATURE = "sha256=757107ea0eb2509fc211221cce984b8a37570b6d7586c22c46f4379c8b043e17";

	private final GitHubWebhookSignatureVerifier verifier = new GitHubWebhookSignatureVerifier(SECRET);

	@Test
	void acceptsGitHubReferenceSignature() {
		assertThat(verifier.isValid(BODY, SIGNATURE)).isTrue();
	}

	@Test
	void acceptsUppercaseHex() {
		assertThat(verifier.isValid(BODY, "sha256=" + SIGNATURE.substring(7).toUpperCase())).isTrue();
	}

	@Test
	void rejectsAlteredBody() {
		assertThat(verifier.isValid("Hello, World?".getBytes(StandardCharsets.UTF_8), SIGNATURE)).isFalse();
	}

	@Test
	void rejectsWrongSecret() {
		assertThat(new GitHubWebhookSignatureVerifier("another secret").isValid(BODY, SIGNATURE)).isFalse();
	}

	@Test
	void rejectsMalformedHeaders() {
		assertThat(verifier.isValid(BODY, null)).isFalse();
		assertThat(verifier.isValid(BODY, "")).isFalse();
		assertThat(verifier.isValid(BODY, SIGNATURE.replace("sha256=", "sha1=xx"))).isFalse();
		assertThat(verifier.isValid(BODY, SIGNATURE.substring(0, SIGNATURE.length() - 1))).isFalse();
		assertThat(verifier.isValid(BODY, SIGNATURE.substring(0, SIGNATURE.length() - 1) + "z")).isFalse();
	}

	@Test
	void unconfiguredVerifierRejectsEverything() {
		GitHubWebhookSignatureVerifier unconfigured = new GitHubWebhookSignatureVerifier(" ");

		assertThat(unconfigured.isConfigured()).isFalse();
		assertThat(unconfigured.isValid(BODY, SIGNATURE)).isFalse();
	}

}
