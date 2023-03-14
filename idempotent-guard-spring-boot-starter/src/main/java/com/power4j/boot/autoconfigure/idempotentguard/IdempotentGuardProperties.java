package com.power4j.boot.autoconfigure.idempotentguard;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@Data
@ConfigurationProperties(prefix = IdempotentGuardProperties.PROP_PREFIX)
public class IdempotentGuardProperties {

	public static final String PROP_PREFIX = "idempotent-guard";

	private boolean enabled = true;

	private GlobalConfig globalConfig = new GlobalConfig();

	private JdbcConfig jdbc = new JdbcConfig();

	@Data
	public static class JdbcConfig {

		/**
		 * 表名称
		 */
		private String tableName = "lock_guard";

		/**
		 * 清理任务的执行频率(秒)
		 */
		private long clearJobDelay = 30;

		/**
		 * 额外的过期延时(秒)
		 */
		private long clearExpireExtra = 30;

	}

	@Data
	public static class GlobalConfig {

		/**
		 * 锁的过期时间(秒)
		 */
		private long lockExpire = 30;

	}

}
