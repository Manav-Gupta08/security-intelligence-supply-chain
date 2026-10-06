package io.secintel.platform.common.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Renders every API failure as {@code application/problem+json} and never exposes exception details.
 */
@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler(Exception.class)
	ResponseEntity<Object> handleUnexpected(Exception ex, WebRequest request) {
		log.error("Unhandled request failure", ex);
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, "An unexpected error occurred.");
		return createResponseEntity(problem, new HttpHeaders(), status, request);
	}

	@Override
	protected ResponseEntity<Object> createResponseEntity(Object body, HttpHeaders headers, HttpStatusCode statusCode,
			WebRequest request) {
		String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
		if (body instanceof ProblemDetail problem && correlationId != null) {
			problem.setProperty("correlationId", correlationId);
		}
		return super.createResponseEntity(body, headers, statusCode, request);
	}

}
