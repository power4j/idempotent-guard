package com.power4j.idempotentguard.api.utils;

import lombok.experimental.UtilityClass;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@UtilityClass
public class EnvUtil {

	private final static AtomicReference<String> HOSTNAME = new AtomicReference<>();

	public Optional<String> getHostname() {
		String name = HOSTNAME.get();
		if (name == null) {
			name = getLocalHostName().orElse(null);
			if (null != name) {
				HOSTNAME.set(name);
			}
		}
		return Optional.ofNullable(name);
	}

	private static Optional<String> getLocalHostName() {
		try {
			return Optional.ofNullable(InetAddress.getLocalHost().getHostName());
		}
		catch (UnknownHostException e) {
			return Optional.empty();
		}
	}

}
