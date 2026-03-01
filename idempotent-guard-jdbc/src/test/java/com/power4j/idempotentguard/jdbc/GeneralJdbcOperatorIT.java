package com.power4j.idempotentguard.jdbc;

import com.power4j.idempotentguard.api.guard.Holder;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeneralJdbcOperatorIT extends H2TestSupport {

	@Test
	void create_shouldInsertRow() {
		Holder holder = operator.create("key-1", Instant.now(), Duration.ofSeconds(30), "holder", null);

		assertThat(holder).isNotNull();
		assertThat(holder.getKey()).isEqualTo("key-1");
		assertThat(((HoldState) holder).getHoldToken()).isNotBlank();
	}

	@Test
	void create_duplicateKey_shouldThrow() {
		operator.create("key-dup", Instant.now(), Duration.ofSeconds(30), "holder", null);

		assertThatThrownBy(() -> operator.create("key-dup", Instant.now(), Duration.ofSeconds(30), "holder", null))
			.isInstanceOf(DuplicateKeyException.class);
	}

	@Test
	void update_whenExpired_shouldReplaceRow() {
		operator.create("key-exp", Instant.now().minusSeconds(120), Duration.ofSeconds(10), "holder", null);

		Optional<Holder> result = operator.update("key-exp", Instant.now(), Duration.ofSeconds(30), "holder2", null);

		assertThat(result).isPresent();
	}

	@Test
	void update_whenNotExpired_shouldReturnEmpty() {
		operator.create("key-active", Instant.now(), Duration.ofSeconds(3600), "holder", null);

		Optional<Holder> result = operator.update("key-active", Instant.now(), Duration.ofSeconds(30), "holder2", null);

		assertThat(result).isEmpty();
	}

	@Test
	void delete_withCorrectToken_shouldRemoveRow() {
		HoldState state = (HoldState) operator.create("key-del", Instant.now(), Duration.ofSeconds(30), "holder", null);

		boolean result = operator.delete("key-del", state.getHoldToken());

		assertThat(result).isTrue();
	}

	@Test
	void delete_withWrongToken_shouldReturnFalse() {
		operator.create("key-wrong", Instant.now(), Duration.ofSeconds(30), "holder", null);

		boolean result = operator.delete("key-wrong", "wrong-token");

		assertThat(result).isFalse();
	}

	@Test
	void clear_shouldRemoveExpiredRows() {
		operator.create("key-old", Instant.now().minusSeconds(120), Duration.ofSeconds(10), "holder", null);
		operator.create("key-new", Instant.now(), Duration.ofSeconds(3600), "holder", null);

		int cleared = operator.clear(Duration.ZERO);

		assertThat(cleared).isEqualTo(1);
		Integer oldCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM lock_guard WHERE id = 'key-old'",
				Integer.class);
		assertThat(oldCount).isZero();
		Integer newCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM lock_guard WHERE id = 'key-new'",
				Integer.class);
		assertThat(newCount).isEqualTo(1);
	}

}
