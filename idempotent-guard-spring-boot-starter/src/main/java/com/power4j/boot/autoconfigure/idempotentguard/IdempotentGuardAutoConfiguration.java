package com.power4j.boot.autoconfigure.idempotentguard;

import com.power4j.idempotentguard.api.guard.AccessGuard;
import com.power4j.idempotentguard.api.guard.KeyEncoder;
import com.power4j.idempotentguard.api.handler.GuardExceptionTranslator;
import com.power4j.idempotentguard.jdbc.ClearJob;
import com.power4j.idempotentguard.jdbc.GeneralJdbcGuard;
import com.power4j.idempotentguard.jdbc.GeneralJdbcOperator;
import com.power4j.idempotentguard.jdbc.JdbcOperator;
import com.power4j.idempotentguard.spring.aspect.DefaultKeyEncoder;
import com.power4j.idempotentguard.spring.aspect.HoldResourceAspect;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(IdempotentGuardProperties.class)
@ConditionalOnProperty(prefix = IdempotentGuardProperties.PROP_PREFIX, name = "enabled")
public class IdempotentGuardAutoConfiguration {

	private final IdempotentGuardProperties properties;

	@Bean
	@ConditionalOnMissingBean
	KeyEncoder keyEncoder() {
		return new DefaultKeyEncoder();
	}

	@Bean
	@ConditionalOnBean({ KeyEncoder.class, GuardExceptionTranslator.class, AccessGuard.class })
	HoldResourceAspect holdResourceAspect(KeyEncoder encoder, GuardExceptionTranslator translator,
			AccessGuard accessGuard) {
		return new HoldResourceAspect(encoder, translator, accessGuard);
	}

	@Configuration
	@ConditionalOnBean(DataSource.class)
	static class JdbcConfigure {

		@Bean
		@ConditionalOnMissingBean
		JdbcOperations jdbcOperations(DataSource dataSource) {
			return new JdbcTemplate(dataSource);
		}

		@Bean
		@ConditionalOnMissingBean
		JdbcOperator jdbcOperator(JdbcOperations jdbcOperations, IdempotentGuardProperties properties) {
			return new GeneralJdbcOperator(jdbcOperations, properties.getJdbc().getTableName());
		}

		@Bean
		@ConditionalOnMissingBean
		@ConditionalOnBean(JdbcOperator.class)
		GeneralJdbcGuard generalJdbcGuard(JdbcOperator jdbcOperator) {
			return new GeneralJdbcGuard(jdbcOperator);
		}

		@Bean
		@ConditionalOnMissingBean
		@ConditionalOnBean(JdbcOperator.class)
		ClearJob clearJob(JdbcOperator jdbcOperator, IdempotentGuardProperties properties) {
			return new ClearJob(jdbcOperator, properties.getJdbc().getClearJobDelay(),
					properties.getJdbc().getExpireDelay());
		}

	}

}
