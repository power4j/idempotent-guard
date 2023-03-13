package com.power4j.idempotentguard.spring.aspect;

import com.power4j.idempotentguard.api.guard.KeyEncoder;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
public class DefaultKeyEncoder implements KeyEncoder {

	private final static String DELIMITER = ",";

	@Override
	public String encode(String operation, List<Object> objects) {
		String context = objects.stream()
			.filter(Objects::nonNull)
			.map(Object::toString)
			.collect(Collectors.joining(DELIMITER));
		context = operation + DELIMITER + context;
		String hex1 = DigestUtils.sha256Hex(context);
		String hex2 = DigestUtils.md5Hex(context);
		return hex1.substring(0, 16) + "-" + hex2.substring(0, 16);
	}

}
