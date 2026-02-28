package com.power4j.idempotentguard.api.exception;

/**
 * 基础设施异常，区别于"锁被占用"的业务语义
 *
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
public class GuardInfrastructureException extends RuntimeException {

	public GuardInfrastructureException(String message, Throwable cause) {
		super(message, cause);
	}

}
