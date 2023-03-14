package com.power4j.idempotentguard.jdbc;

import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@Slf4j
public class ClearJob implements Closeable {

	private final static long EXEC_DELAY_LOW = 10;

	private final static long EXTRA_EXPIRE_LOW = 30;

	private final ScheduledExecutorService executorService;

	public ClearJob(JdbcOperator jdbcOperator, long executeDelay, long extraExpire) {
		if (executeDelay < EXEC_DELAY_LOW) {
			log.warn("清理任务执行间隔过低:{} s", executeDelay);
		}
		if (extraExpire < EXTRA_EXPIRE_LOW) {
			log.warn("额外过期清除时间过低:{} s", extraExpire);
		}
		this.executorService = new ScheduledThreadPoolExecutor(1);
		this.executorService.schedule(() -> clear(jdbcOperator, extraExpire), executeDelay, TimeUnit.SECONDS);
	}

	private void clear(JdbcOperator jdbcOperator, long extraExpire) {
		try {
			int count = jdbcOperator.clear(Duration.ofSeconds(extraExpire));
			if (log.isDebugEnabled()) {
				log.debug("GeneralJdbcOperator clear,count = {}", count);
			}
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void close() throws IOException {
		executorService.shutdownNow();
	}

}
