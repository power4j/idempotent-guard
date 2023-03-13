package com.power4j.idempotentguard.api.handler;

import com.power4j.idempotentguard.api.exception.ResourceGuardException;
import org.springframework.lang.Nullable;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
public interface GuardExceptionTranslator {

	/**
	 * 异常处理,此方法可以返回一个值作为返回给前端响应,也可以抛出异常
	 * @param e 原始异常
	 * @return 返回的对象作为新的返回值
	 */
	@Nullable
	Object translate(ResourceGuardException e);

}
