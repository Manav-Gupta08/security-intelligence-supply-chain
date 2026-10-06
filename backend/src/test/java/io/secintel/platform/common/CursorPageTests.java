package io.secintel.platform.common;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class CursorPageTests {

	private static final String SCOPE = "items:key";

	@Test
	void lastPageHasNoCursor() {
		CursorPage<String> page = CursorPage.of(List.of("a", "b"), 2, SCOPE, Function.identity());

		assertThat(page.items()).containsExactly("a", "b");
		assertThat(page.page().nextCursor()).isNull();
	}

	@Test
	void extraRowProducesCursorForLastReturnedItem() {
		CursorPage<String> page = CursorPage.of(List.of("a", "b", "c"), 2, SCOPE, Function.identity());

		assertThat(page.items()).containsExactly("a", "b");
		assertThat(CursorPage.decodeCursor(SCOPE, page.page().nextCursor())).isEqualTo("b");
	}

	@Test
	void absentCursorDecodesToNull() {
		assertThat(CursorPage.decodeCursor(SCOPE, null)).isNull();
	}

	@Test
	void rejectsCursorFromAnotherScope() {
		String cursor = CursorPage.of(List.of("a", "b"), 1, "other:key", Function.identity()).page().nextCursor();

		assertThatExceptionOfType(ApiException.class).isThrownBy(() -> CursorPage.decodeCursor(SCOPE, cursor));
	}

	@Test
	void rejectsMalformedCursors() {
		String emptyKey = Base64.getUrlEncoder().encodeToString((SCOPE + "\n").getBytes(StandardCharsets.UTF_8));

		for (String cursor : List.of("", "not base64!", "a".repeat(513), emptyKey)) {
			assertThatExceptionOfType(ApiException.class).isThrownBy(() -> CursorPage.decodeCursor(SCOPE, cursor));
		}
	}

}
