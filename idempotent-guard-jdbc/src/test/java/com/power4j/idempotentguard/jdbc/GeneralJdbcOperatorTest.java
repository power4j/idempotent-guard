package com.power4j.idempotentguard.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcOperations;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeneralJdbcOperatorTest {

	@Mock
	JdbcOperations jdbcOperations;

	GeneralJdbcOperator operator;

	@BeforeEach
	void setUp() {
		operator = new GeneralJdbcOperator(jdbcOperations, "lock_guard");
	}

	@Test
	void clear_thresholdShouldBeBeforeNow() {
		Instant before = Instant.now();
		operator.clear(Duration.ofSeconds(30));

		ArgumentCaptor<Timestamp> captor = ArgumentCaptor.forClass(Timestamp.class);
		verify(jdbcOperations).update(anyString(), captor.capture());
		assertThat(captor.getValue().toInstant()).isBefore(before);
	}

	@Test
	void delete_shouldPassIdAndToken() {
		when(jdbcOperations.update(anyString(), anyString(), anyString())).thenReturn(1);

		boolean result = operator.delete("key-1", "token-abc");

		verify(jdbcOperations).update(anyString(), eq("key-1"), eq("token-abc"));
		assertThat(result).isTrue();
	}

	@Test
	void delete_shouldReturnFalse_whenNoRowAffected() {
		when(jdbcOperations.update(anyString(), anyString(), anyString())).thenReturn(0);

		boolean result = operator.delete("key-1", "stale-token");

		assertThat(result).isFalse();
	}

}
