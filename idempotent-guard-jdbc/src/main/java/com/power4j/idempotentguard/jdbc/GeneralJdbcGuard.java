package com.power4j.idempotentguard.jdbc;

import com.power4j.idempotentguard.api.guard.AccessGuard;
import com.power4j.idempotentguard.api.guard.HoldRequest;
import com.power4j.idempotentguard.api.guard.Holder;
import com.power4j.idempotentguard.api.utils.EnvUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;

import java.time.Instant;
import java.util.Optional;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
public class GeneralJdbcGuard implements AccessGuard {

	private final JdbcOperator jdbcOperator;

	@Override
	public Optional<Holder> tryHold(HoldRequest request) {
		String id = request.getKey();
		try {
			Holder result = jdbcOperator.create(id, Instant.now(), request.getDuration(), getHolder(),
					request.getHint());
			return Optional.of(result);
		}
		catch (DuplicateKeyException e) {
			return jdbcOperator.update(id, Instant.now(), request.getDuration(), getHolder(), request.getHint());
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
			return Optional.empty();
		}

	}

	@Override
	public void release(Holder holder) {
		jdbcOperator.delete(holder.getKey());
	}

	protected String getHolder() {
		return Thread.currentThread().getName() + "@" + EnvUtil.getHostname().orElse("unknown hostname");
	}

}
