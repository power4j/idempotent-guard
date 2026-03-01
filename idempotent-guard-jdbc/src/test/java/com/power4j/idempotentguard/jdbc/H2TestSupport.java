package com.power4j.idempotentguard.jdbc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

abstract class H2TestSupport {

	private EmbeddedDatabase db;

	protected JdbcTemplate jdbcTemplate;

	protected GeneralJdbcOperator operator;

	@BeforeEach
	void setUpDatabase() {
		db = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).addScript("db/schema-h2.sql").build();
		jdbcTemplate = new JdbcTemplate(db);
		operator = new GeneralJdbcOperator(jdbcTemplate, "lock_guard");
	}

	@AfterEach
	void tearDownDatabase() {
		db.shutdown();
	}

}
