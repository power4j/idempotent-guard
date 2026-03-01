package com.power4j.idempotentguard.jdbc;

import com.power4j.idempotentguard.api.guard.HoldRequest;
import com.power4j.idempotentguard.api.guard.Holder;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class GeneralJdbcGuardIT extends H2TestSupport {

	private HoldRequest requestFor(String key) {
		return HoldRequest.builder().key(key).operation("test-op").duration(Duration.ofSeconds(30)).build();
	}

	@Test
	void tryHold_newKey_shouldSucceed() {
		GeneralJdbcGuard guard = new GeneralJdbcGuard(operator);

		Optional<Holder> result = guard.tryHold(requestFor("order-1"));

		assertThat(result).isPresent();
	}

	@Test
	void tryHold_sameKey_notExpired_shouldFail() {
		GeneralJdbcGuard guard = new GeneralJdbcGuard(operator);
		guard.tryHold(requestFor("order-2"));

		Optional<Holder> second = guard.tryHold(requestFor("order-2"));

		assertThat(second).isEmpty();
	}

	@Test
	void tryHold_sameKey_afterExpiry_shouldSucceed() {
		GeneralJdbcGuard guard = new GeneralJdbcGuard(operator);
		guard.tryHold(requestFor("order-3"));
		jdbcTemplate.update("UPDATE lock_guard SET expire_time_utc = ? WHERE id = ?",
				Timestamp.from(Instant.now().minusSeconds(60)), "order-3");

		Optional<Holder> result = guard.tryHold(requestFor("order-3"));

		assertThat(result).isPresent();
	}

	@Test
	void release_shouldAllowRehold() {
		GeneralJdbcGuard guard = new GeneralJdbcGuard(operator);
		Holder holder = guard.tryHold(requestFor("order-4")).orElseThrow();
		guard.release(holder);

		Optional<Holder> result = guard.tryHold(requestFor("order-4"));

		assertThat(result).isPresent();
	}

}
