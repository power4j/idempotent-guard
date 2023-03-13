package com.power4j.idempotentguard.api.guard;

import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.Nullable;

import java.time.Duration;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@Getter
@Builder
public class HoldRequest {

	private final String operation;

	private final String key;

	private final Duration duration;

	@Nullable
	private final String hint;

}
