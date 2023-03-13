package com.power4j.idempotentguard.api.guard;

import java.util.List;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
public interface KeyEncoder {

	/**
	 * Key编码
	 * @param operation 资源操作符
	 * @param objects 资源集合,可能有空值
	 * @return 返回资源集合的唯一key
	 */
	String encode(String operation, List<Object> objects);

}
