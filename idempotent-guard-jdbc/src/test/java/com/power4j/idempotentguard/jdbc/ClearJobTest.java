package com.power4j.idempotentguard.jdbc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ClearJobTest {

	@Mock
	JdbcOperator jdbcOperator;

	@Mock
	ScheduledExecutorService executorService;

	@Test
	void clearJob_shouldSchedulePeriodically() {
		new ClearJob(jdbcOperator, 30, 30, executorService);

		verify(executorService).scheduleAtFixedRate(any(Runnable.class), eq(30L), eq(30L), eq(TimeUnit.SECONDS));
	}

}
