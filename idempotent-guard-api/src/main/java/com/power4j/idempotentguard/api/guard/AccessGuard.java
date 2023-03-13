package com.power4j.idempotentguard.api.guard;

import java.util.Optional;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
public interface AccessGuard {

	/**
	 * 尝试占用资源
	 * @param request 占用请求
	 * @return 失败返回empty
	 */
	Optional<Holder> tryHold(HoldRequest request);

	/**
	 * 释放
	 * @param holder 状态信息
	 */
	void release(Holder holder);

}
