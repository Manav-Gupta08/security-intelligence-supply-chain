package io.secintel.platform.common;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;

/**
 * Keyset-paginated collection response: {@code {"items": [...], "page": {"nextCursor": "..."}}}.
 * Cursors are opaque to clients and bound to the scope (resource and sort key) that issued them.
 */
public record CursorPage<T>(List<T> items, PageInfo page) {

	public static final String DEFAULT_LIMIT = "50";

	public static final int MAX_LIMIT = 100;

	private static final int MAX_CURSOR_LENGTH = 512;

	private static final char SCOPE_SEPARATOR = '\n';

	public record PageInfo(String nextCursor) {
	}

	/**
	 * Builds a page from a result fetched with {@code limit + 1} rows; the extra row only signals that
	 * another page exists.
	 */
	public static <T> CursorPage<T> of(List<T> fetched, int limit, String scope, Function<T, String> sortKey) {
		if (fetched.size() <= limit) {
			return new CursorPage<>(List.copyOf(fetched), new PageInfo(null));
		}
		List<T> items = List.copyOf(fetched.subList(0, limit));
		return new CursorPage<>(items, new PageInfo(encode(scope, sortKey.apply(items.get(limit - 1)))));
	}

	/**
	 * Returns the sort key carried by {@code cursor}, or {@code null} when no cursor was supplied.
	 * @throws ApiException if the cursor is malformed or was issued for a different scope
	 */
	public static String decodeCursor(String scope, String cursor) {
		if (cursor == null) {
			return null;
		}
		if (cursor.isEmpty() || cursor.length() > MAX_CURSOR_LENGTH) {
			throw invalidCursor();
		}
		String decoded;
		try {
			decoded = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
		}
		catch (IllegalArgumentException ex) {
			throw invalidCursor();
		}
		String prefix = scope + SCOPE_SEPARATOR;
		if (!decoded.startsWith(prefix) || decoded.length() == prefix.length()) {
			throw invalidCursor();
		}
		return decoded.substring(prefix.length());
	}

	public <R> CursorPage<R> map(Function<T, R> mapper) {
		return new CursorPage<>(items.stream().map(mapper).toList(), page);
	}

	private static String encode(String scope, String key) {
		byte[] raw = (scope + SCOPE_SEPARATOR + key).getBytes(StandardCharsets.UTF_8);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
	}

	private static ApiException invalidCursor() {
		return ApiException.badRequest("The pagination cursor is invalid.");
	}

}
