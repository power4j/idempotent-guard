package com.power4j.idempotentguard.jdbc;

import com.power4j.idempotentguard.api.guard.Holder;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
public interface JdbcOperator {

	/**
	 * 创建记录,获取所有权
	 * @param id
	 * @param startTime
	 * @param duration
	 * @param holder
	 * @param hint
	 * @return
	 * @throws org.springframework.dao.DataAccessException 创建失败,比如主键冲突
	 */
	Holder create(String id, Instant startTime, Duration duration, String holder, @Nullable String hint);

	/**
	 * 更新记录,获取所有权
	 * @param id
	 * @param startTime
	 * @param duration
	 * @param holder
	 * @param hint
	 * @return
	 */
	Optional<Holder> update(String id, Instant startTime, Duration duration, String holder, @Nullable String hint);

	/**
	 * 删除记录
	 * @param id
	 * @return
	 */
	boolean delete(String id);

	/**
	 * 清理
	 * @param extra 删除额外延时
	 * @return
	 */
	int clear(Duration extra);

}
