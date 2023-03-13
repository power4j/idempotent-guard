package com.power4j.idempotentguard.api.utils;

import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@UtilityClass
public class StringUtil {

	@Nullable
	public String truncate(@Nullable String src, int length) {
		if (src == null) {
			return null;
		}
		if (src.isEmpty()) {
			return src;
		}
		return src.substring(0, Math.min(src.length(), length));
	}

}
