package io.secintel.platform.integration.github;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.unit.DataSize;

/**
 * @param secret shared secret configured on the GitHub App; blank disables the webhook endpoint
 * @param maxPayloadSize largest accepted delivery body
 */
@ConfigurationProperties("secintel.github.webhook")
record GitHubWebhookProperties(@DefaultValue("") String secret, @DefaultValue("25MB") DataSize maxPayloadSize) {

	GitHubWebhookProperties {
		long bytes = maxPayloadSize.toBytes();
		if (bytes < 1 || bytes >= Integer.MAX_VALUE) {
			throw new IllegalArgumentException("secintel.github.webhook.max-payload-size must be between 1B and 2GB");
		}
	}

	int maxPayloadBytes() {
		return (int) maxPayloadSize.toBytes();
	}

	@Override
	public String toString() {
		return "GitHubWebhookProperties[secret=" + (secret.isBlank() ? "<unset>" : "<redacted>") + ", maxPayloadSize="
				+ maxPayloadSize + "]";
	}

}
