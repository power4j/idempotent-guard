package com.power4j.idempotentguard.jdbc;

import com.power4j.idempotentguard.api.exception.GuardInfrastructureException;
import com.power4j.idempotentguard.api.guard.HoldRequest;
import com.power4j.idempotentguard.api.guard.Holder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeneralJdbcGuardTest {

	@Mock
	JdbcOperator jdbcOperator;

	GeneralJdbcGuard guard;

	HoldRequest request;

	@BeforeEach
	void setUp() {
		guard = new GeneralJdbcGuard(jdbcOperator);
		request = HoldRequest.builder()
			.key("order-123")
			.operation("place-order")
			.duration(Duration.ofSeconds(30))
			.build();
	}

	@Test
	void tryHold_success() {
		HoldState state = HoldState.builder()
			.id("order-123")
			.createAt(Instant.now())
			.deadline(Instant.now().plusSeconds(30))
			.holdBy("t1")
			.holdToken("tok-1")
			.build();
		when(jdbcOperator.create(anyString(), any(), any(), anyString(), isNull())).thenReturn(state);

		Optional<Holder> result = guard.tryHold(request);

		assertThat(result).isPresent();
	}

	@Test
	void tryHold_duplicate_thenUpdate() {
		HoldState state = HoldState.builder()
			.id("order-123")
			.createAt(Instant.now())
			.deadline(Instant.now().plusSeconds(30))
			.holdBy("t2")
			.holdToken("tok-2")
			.build();
		when(jdbcOperator.create(anyString(), any(), any(), anyString(), isNull()))
			.thenThrow(new DuplicateKeyException("dup"));
		when(jdbcOperator.update(anyString(), any(), any(), anyString(), isNull())).thenReturn(Optional.of(state));

		Optional<Holder> result = guard.tryHold(request);

		assertThat(result).isPresent();
	}

	@Test
	void tryHold_dbError_throwsInfraException() {
		when(jdbcOperator.create(anyString(), any(), any(), anyString(), isNull()))
			.thenThrow(new RuntimeException("connection refused"));

		assertThatThrownBy(() -> guard.tryHold(request)).isInstanceOf(GuardInfrastructureException.class);
	}

	@Test
	void release_shouldPassTokenToDelete() {
		HoldState state = HoldState.builder()
			.id("order-123")
			.createAt(Instant.now())
			.deadline(Instant.now().plusSeconds(30))
			.holdBy("t1")
			.holdToken("tok-release")
			.build();

		guard.release(state);

		verify(jdbcOperator).delete(eq("order-123"), eq("tok-release"));
	}

}
