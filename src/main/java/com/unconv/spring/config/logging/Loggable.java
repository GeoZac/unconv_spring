package com.unconv.spring.config.logging;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to indicate that a class or method should be logged by the logging aspect.
 *
 * <p>When a class or method is annotated with {@code @Loggable}, the logging aspect will log the
 * entry, exit, and any exceptions thrown by the annotated method or methods within the annotated
 * class.
 *
 * <p>This annotation can be applied at both the class and method level. If applied at the class
 * level, all methods within the class will be subject to logging.
 *
 * <p>The {@code @Loggable} annotation is inherited, so if a class is annotated with
 * {@code @Loggable}, subclasses will also inherit the logging behavior unless explicitly
 * overridden.
 *
 * @see com.unconv.spring.config.logging.LoggingAspect
 */
@Target({TYPE, METHOD})
@Retention(RUNTIME)
@Inherited
public @interface Loggable {}
