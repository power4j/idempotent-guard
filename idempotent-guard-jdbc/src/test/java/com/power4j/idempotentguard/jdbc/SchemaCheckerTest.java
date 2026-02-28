package com.power4j.idempotentguard.jdbc;

import com.power4j.idempotentguard.api.exception.GuardInfrastructureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcOperations;

import java.sql.SQLException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchemaCheckerTest {

	@Mock
	JdbcOperations jdbcOperations;

	@Test
	void check_shouldPassWhenQuerySucceeds() {
		when(jdbcOperations.queryForList(anyString())).thenReturn(Collections.emptyList());
		SchemaChecker checker = new SchemaChecker(jdbcOperations, "lock_guard");
		assertThatNoException().isThrownBy(checker::afterPropertiesSet);
	}

	@Test
	void check_shouldThrowInfraException_whenColumnMissing() {
		when(jdbcOperations.queryForList(anyString()))
			.thenThrow(new BadSqlGrammarException("query", "SELECT...", new SQLException("column not found")));
		SchemaChecker checker = new SchemaChecker(jdbcOperations, "lock_guard");
		assertThatThrownBy(checker::afterPropertiesSet).isInstanceOf(GuardInfrastructureException.class)
			.hasMessageContaining("Schema check failed");
	}

}
