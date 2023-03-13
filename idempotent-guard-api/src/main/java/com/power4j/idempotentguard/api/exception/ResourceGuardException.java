package com.power4j.idempotentguard.api.exception;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
public class ResourceGuardException extends RuntimeException {

	public ResourceGuardException(String message) {
		super(message);
	}

	public ResourceGuardException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResourceGuardException(Throwable cause) {
		super(cause);
	}

}
