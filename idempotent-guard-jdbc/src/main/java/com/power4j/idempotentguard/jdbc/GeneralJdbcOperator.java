package com.power4j.idempotentguard.jdbc;

import com.power4j.idempotentguard.api.guard.Holder;
import com.power4j.idempotentguard.api.utils.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.lang.Nullable;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
public class GeneralJdbcOperator implements JdbcOperator {

	private final static String TPL_INSERT = "INSERT INTO %s (id,start_time_utc,expire_time_utc,hold_by,hold_hint)"
			+ "VALUES (?,?,?,?,?)";

	private final static String TPL_UPDATE = "UPDATE %s SET start_time_utc = ?,expire_time_utc = ? ,hold_by = ?,hold_hint = ?"
			+ "WHERE id = ? AND expire_time_utc < ?";

	private final static String TPL_DELETE = "DELETE FROM %s WHERE id = ?";

	private final static String TPL_CLEAR = "DELETE FROM %s WHERE expire_time_utc < ?";

	private final JdbcOperations jdbcOperations;

	private final String tableName;

	@Override
	public Holder create(String id, Instant startTime, Duration duration, String holder, @Nullable String hint) {
		String sql = String.format(TPL_INSERT, tableName);
		Instant deadline = startTime.plusMillis(duration.toMillis());
		// @formatter:off
		int row = jdbcOperations.update(sql,
				id,
				new Timestamp(startTime.toEpochMilli()),
				new Timestamp(deadline.toEpochMilli()),
				StringUtil.truncate(holder, 255),
				StringUtil.truncate(hint, 255)
		);
		// @formatter:on
		if (row <= 0) {
			throw new IllegalStateException("Insert row success,not row effected");
		}
		// @formatter:off
		return HoldState.builder()
				.id(id)
				.createAt(startTime)
				.deadline(deadline)
				.holdBy(holder)
				.holdHint(hint)
				.build();
		// @formatter:on
	}

	@Override
	public Optional<Holder> update(String id, Instant startTime, Duration duration, String holder,
			@Nullable String hint) {
		String sql = String.format(TPL_UPDATE, tableName);
		Instant deadline = startTime.plusMillis(duration.toMillis());
		// @formatter:off
		int row = jdbcOperations.update(sql,
				new Timestamp(startTime.toEpochMilli()),
				new Timestamp(deadline.toEpochMilli()),
				StringUtil.truncate(holder, 255),
				StringUtil.truncate(hint, 255),
				id,
				new Timestamp(Instant.now().toEpochMilli())
		);
		// @formatter:on
		HoldState state = null;
		if (row > 0) {
			// @formatter:off
			state = HoldState.builder()
					.id(id)
					.createAt(startTime)
					.deadline(deadline)
					.holdBy(holder)
					.holdHint(hint)
					.build();
			// @formatter:on
		}
		return Optional.ofNullable(state);
	}

	@Override
	public boolean delete(String id) {
		String sql = String.format(TPL_DELETE, tableName);
		int row = jdbcOperations.update(sql, id);
		return row > 0;
	}

	@Override
	public int clear(Duration extra) {
		String sql = String.format(TPL_CLEAR, tableName);
		Instant time = Instant.now().plusMillis(extra.toMillis());
		return jdbcOperations.update(sql, time);
	}

}
