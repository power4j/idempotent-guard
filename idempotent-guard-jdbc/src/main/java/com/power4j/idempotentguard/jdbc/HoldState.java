package com.power4j.idempotentguard.jdbc;

import com.power4j.idempotentguard.api.guard.Holder;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@Getter
@Builder
public class HoldState implements Holder {

	private final String id;

	private final String holdBy;

	private final String holdHint;

	private final Instant createAt;

	private final Instant deadline;

	@Override
	public String getKey() {
		return id;
	}

	@Override
	public String getHint() {
		return holdHint;
	}

}
