package io.secintel.platform.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Client-facing failure rendered as an RFC 9457 problem. The detail must be safe to expose.
 */
public class ApiException extends ErrorResponseException {

	private static final long serialVersionUID = 1L;

	public ApiException(HttpStatus status, String detail) {
		super(status, ProblemDetail.forStatusAndDetail(status, detail), null);
	}

	public static ApiException notFound() {
		return new ApiException(HttpStatus.NOT_FOUND, "The requested resource was not found.");
	}

	public static ApiException badRequest(String detail) {
		return new ApiException(HttpStatus.BAD_REQUEST, detail);
	}

}
