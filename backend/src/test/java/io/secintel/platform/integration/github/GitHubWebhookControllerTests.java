package io.secintel.platform.integration.github;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GitHubWebhookController.class,
		properties = { "secintel.github.webhook.secret=" + GitHubWebhookControllerTests.SECRET,
				"secintel.github.webhook.max-payload-size=1KB" })
@Import(GitHubWebhookConfiguration.class)
class GitHubWebhookControllerTests {

	static final String SECRET = "test-webhook-secret";

	private static final String PATH = "/api/v1/webhooks/github";

	private static final String DELIVERY_ID = "72d3162e-cc78-11e3-81ab-4c9367dc0958";

	private static final byte[] BODY = "{\"action\":\"opened\",\"installation\":{\"id\":42}}"
		.getBytes(StandardCharsets.UTF_8);

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private GitHubWebhookDeliveryService deliveries;

	@Test
	void acceptsNewSignedDelivery() throws Exception {
		given(deliveries.accept("issues", DELIVERY_ID, BODY)).willReturn(GitHubWebhookDeliveryService.Outcome.ACCEPTED);

		mvc.perform(signed(BODY)).andExpect(status().isAccepted()).andExpect(content().string(""));

		verify(deliveries).accept(eq("issues"), eq(DELIVERY_ID), eq(BODY));
	}

	@Test
	void acknowledgesRedeliveryWithoutError() throws Exception {
		given(deliveries.accept(any(), any(), any())).willReturn(GitHubWebhookDeliveryService.Outcome.DUPLICATE);

		mvc.perform(signed(BODY)).andExpect(status().isOk());
	}

	@Test
	void rejectsMissingSignatureBeforeProcessing() throws Exception {
		mvc.perform(unsigned(BODY))
			.andExpect(status().isUnauthorized())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(header().exists("X-Correlation-Id"))
			.andExpect(jsonPath("$.correlationId").isNotEmpty());

		verifyNoInteractions(deliveries);
	}

	@Test
	void rejectsSignatureOverDifferentBody() throws Exception {
		byte[] tampered = "{\"action\":\"deleted\",\"installation\":{\"id\":42}}".getBytes(StandardCharsets.UTF_8);

		mvc.perform(unsigned(tampered).header("X-Hub-Signature-256", sign(BODY)))
			.andExpect(status().isUnauthorized());

		verifyNoInteractions(deliveries);
	}

	@Test
	void rejectsOversizedPayload() throws Exception {
		byte[] oversized = ("{\"action\":\"" + "a".repeat(1100) + "\"}").getBytes(StandardCharsets.UTF_8);

		mvc.perform(signed(oversized)).andExpect(status().is(413));

		verifyNoInteractions(deliveries);
	}

	@Test
	void rejectsInvalidDeliveryHeaders() throws Exception {
		mvc.perform(unsigned(BODY).header("X-Hub-Signature-256", sign(BODY))
			.header("X-GitHub-Event", "Issues; drop")
			.header("X-GitHub-Delivery", DELIVERY_ID)).andExpect(status().isBadRequest());
		mvc.perform(unsigned(BODY).header("X-Hub-Signature-256", sign(BODY)).header("X-GitHub-Event", "issues"))
			.andExpect(status().isBadRequest());

		verifyNoInteractions(deliveries);
	}

	@Test
	void rejectsNonJsonContentType() throws Exception {
		mvc.perform(signed(BODY).contentType(MediaType.APPLICATION_FORM_URLENCODED))
			.andExpect(status().isUnsupportedMediaType());

		verifyNoInteractions(deliveries);
	}

	private static MockHttpServletRequestBuilder unsigned(byte[] body) {
		return post(PATH).contentType(MediaType.APPLICATION_JSON).content(body);
	}

	private static MockHttpServletRequestBuilder signed(byte[] body) throws Exception {
		return unsigned(body).header("X-Hub-Signature-256", sign(body))
			.header("X-GitHub-Event", "issues")
			.header("X-GitHub-Delivery", DELIVERY_ID);
	}

	private static String sign(byte[] body) throws Exception {
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
		return "sha256=" + HexFormat.of().formatHex(mac.doFinal(body));
	}

}
