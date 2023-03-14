package com.power4j.idempotentguard.spring.aspect;

import com.power4j.fist.support.spring.aop.AopUtil;
import com.power4j.fist.support.spring.spel.MethodParameterResolver;
import com.power4j.fist.support.spring.spel.SpringElUtil;
import com.power4j.fist.support.spring.spel.VariableProvider;
import com.power4j.idempotentguard.api.annotation.LockGuard;
import com.power4j.idempotentguard.api.exception.ResourceGuardException;
import com.power4j.idempotentguard.api.guard.AccessGuard;
import com.power4j.idempotentguard.api.guard.HoldRequest;
import com.power4j.idempotentguard.api.guard.Holder;
import com.power4j.idempotentguard.api.guard.KeyEncoder;
import com.power4j.idempotentguard.api.handler.GuardExceptionTranslator;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
public class HoldResourceAspect {

	private final KeyEncoder keyEncoder;

	private final GuardExceptionTranslator guardExceptionTranslator;

	private final AccessGuard accessGuard;

	@Setter
	private Duration defaultExpire = Duration.ofSeconds(30);

	@Around("@annotation(annotation)")
	public Object around(ProceedingJoinPoint point, LockGuard annotation) throws Throwable {

		String operation = annotation.operation();

		List<Pair<String, Object>> reslist = resolveResource(point, annotation);
		String key = keyEncoder.encode(operation, reslist.stream().map(Pair::getValue).collect(Collectors.toList()));

		String hint = reslist.stream().map(p -> p.getKey() + ":" + p.getValue()).collect(Collectors.joining(","));

		Duration expire = defaultExpire;
		if (annotation.time() > 0) {
			expire = Duration.ofMillis(annotation.timeUnit().toMillis(annotation.time()));
		}

		HoldRequest request = HoldRequest.builder().key(key).operation(operation).hint(hint).duration(expire).build();

		final Holder holder = accessGuard.tryHold(request).orElse(null);
		if (holder == null) {
			return guardExceptionTranslator
				.translate(new ResourceGuardException("Resource hold failed,operation:" + operation));
		}

		try {
			return point.proceed();
		}
		finally {
			try {
				accessGuard.release(holder);
			}
			catch (Exception e) {
				String msg = String.format("release resource error,key = %s, hint = %s", holder.getKey(),
						holder.getHint());
				log.error(msg, e);
			}
		}
	}

	List<Pair<String, Object>> resolveResource(ProceedingJoinPoint point, LockGuard annotation) {

		Object[] arguments = point.getArgs();
		Method method = AopUtil.getMethod(point);
		VariableProvider variableProvider = MethodParameterResolver.of(method, arguments);

		// @formatter:off
		return  Arrays.stream(annotation.resources())
				.map(r -> Pair.of(r.name(), eval(variableProvider,r.expr(),Object.class)))
				.collect(Collectors.toList());
		// @formatter:on

	}

	@Nullable
	<T> T eval(VariableProvider variableProvider, @Nullable String expr, Class<T> clazz) {
		if (StringUtils.isEmpty(expr)) {
			return null;
		}
		return SpringElUtil.evalWithVariable(variableProvider, null, expr, clazz);
	}

}
