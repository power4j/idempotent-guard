package com.power4j.idempotentguard.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@Inherited
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LockGuard {

	/**
	 * 需要保护的操作
	 * @return 操作描述符
	 */
	String operation() default "";

	/**
	 * 需要保护的资源是一组对象
	 * @return 返回对象的EL表达式
	 */
	ResDescribe[] resources() default {};

	/**
	 * 资源自动释放时间
	 * @return 时间值
	 */
	int time() default 0;

	/**
	 * 时间的单位
	 * @return TimeUnit
	 */
	TimeUnit timeUnit() default TimeUnit.SECONDS;

}
