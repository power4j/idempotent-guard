package com.power4j.idempotentguard.api.guard;

import org.springframework.lang.Nullable;

import java.time.Instant;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
public interface Holder {

	/**
	 * 资源的Key
	 * @return
	 */
	String getKey();

	/**
	 * 提示信息
	 * @return
	 */
	@Nullable
	String getHint();

	/**
	 * 过期时间
	 * @return
	 */
	Instant getDeadline();

	/**
	 * 开始时间
	 * @return
	 */
	Instant getCreateAt();

}
