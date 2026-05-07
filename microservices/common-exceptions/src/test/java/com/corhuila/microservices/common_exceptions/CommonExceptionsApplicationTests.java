package com.corhuila.microservices.common_exceptions;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CommonExceptionsApplicationTests {

	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

	@Test
	void shouldReturnGenericErrorResponseForUnhandledException() {
		var response = handler.handleException(new RuntimeException("boom"));

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(
				"An error has occurred. Please contact the administrator or try again later.",
				response.getBody().error().get("message")
		);
	}

	@Test
	void shouldStoreErrorsInResponseRecord() {
		var response = new ErrorResponse(Map.of("field", "message"));

		assertEquals("message", response.error().get("field"));
	}

}
