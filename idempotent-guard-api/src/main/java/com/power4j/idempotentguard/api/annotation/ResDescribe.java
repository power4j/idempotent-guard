package com.power4j.idempotentguard.api.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@Inherited
@Target({ ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ResDescribe {

	/**
	 * 资源名称
	 * @return
	 */
	String name();

	/**
	 * 资源对象的表达式
	 * @return
	 */
	String expr();

}
