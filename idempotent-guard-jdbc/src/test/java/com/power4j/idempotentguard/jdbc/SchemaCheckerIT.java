package com.power4j.idempotentguard.jdbc;

import com.power4j.idempotentguard.api.exception.GuardInfrastructureException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SchemaCheckerIT extends H2TestSupport {

	@Test
	void check_withCorrectSchema_shouldPass() {
		SchemaChecker checker = new SchemaChecker(jdbcTemplate, "lock_guard");

		assertThatNoException().isThrownBy(checker::afterPropertiesSet);
	}

	@Test
	void check_withMissingColumn_shouldThrow() {
		jdbcTemplate.execute("ALTER TABLE lock_guard DROP COLUMN hold_token");
		SchemaChecker checker = new SchemaChecker(jdbcTemplate, "lock_guard");

		assertThatThrownBy(checker::afterPropertiesSet).isInstanceOf(GuardInfrastructureException.class)
			.hasMessageContaining("Schema check failed");
	}

}
