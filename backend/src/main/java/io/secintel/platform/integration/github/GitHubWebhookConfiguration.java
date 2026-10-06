package io.secintel.platform.integration.github;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(GitHubWebhookProperties.class)
class GitHubWebhookConfiguration {

	@Bean
	GitHubWebhookSignatureVerifier gitHubWebhookSignatureVerifier(GitHubWebhookProperties properties) {
		return new GitHubWebhookSignatureVerifier(properties.secret());
	}

}
