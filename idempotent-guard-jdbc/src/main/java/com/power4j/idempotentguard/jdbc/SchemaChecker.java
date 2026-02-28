package com.power4j.idempotentguard.jdbc;

import com.power4j.idempotentguard.api.exception.GuardInfrastructureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcOperations;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
public class SchemaChecker implements InitializingBean {

	private final JdbcOperations jdbcOperations;

	private final String tableName;

	@Override
	public void afterPropertiesSet() {
		String sql = String.format("SELECT id, hold_token FROM %s WHERE 1=0", tableName);
		try {
			jdbcOperations.queryForList(sql);
			log.info("Schema check passed for table: {}", tableName);
		}
		catch (Exception e) {
			throw new GuardInfrastructureException(
					String.format("Schema check failed for table '%s': %s. Please run the migration script.", tableName,
							e.getMessage()),
					e);
		}
	}

}
