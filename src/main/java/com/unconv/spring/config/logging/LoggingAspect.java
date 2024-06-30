package com.unconv.spring.config.logging;

import com.unconv.spring.consts.AppConstants;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

/**
 * Aspect for logging execution of service and repository Spring components.
 *
 * <p>This aspect is applied to all classes annotated with {@link
 * org.springframework.stereotype.Repository}, {@link org.springframework.stereotype.Service}, and
 * {@link org.springframework.web.bind.annotation.RestController}. It also applies to classes and
 * methods annotated with {@link com.unconv.spring.config.logging.Loggable}.
 *
 * <p>The logging aspect logs method entry and exit as well as exceptions thrown by the methods.
 *
 * <p>The logs include detailed information when the application is not running in the production
 * profile.
 */
@Aspect
@Component
public class LoggingAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final Environment env;

    /**
     * Constructs a new {@code LoggingAspect}.
     *
     * @param env the Spring {@link Environment} used to check the current active profiles
     */
    public LoggingAspect(Environment env) {
        this.env = env;
    }

    /** Pointcut that matches all repositories, services, and REST controllers. */
    @Pointcut(
            "within(@org.springframework.stereotype.Repository *)"
                    + " || within(@org.springframework.stereotype.Service *)"
                    + " || within(@org.springframework.web.bind.annotation.RestController *)")
    public void springBeanPointcut() {
        // Pointcut definition
    }

    /**
     * Pointcut that matches all classes and methods annotated with {@link
     * com.unconv.spring.config.logging.Loggable}.
     */
    @Pointcut(
            "@within(com.unconv.spring.config.logging.Loggable) || "
                    + "@annotation(com.unconv.spring.config.logging.Loggable)")
    public void applicationPackagePointcut() {
        // Pointcut definition
    }

    /**
     * Advice that logs methods throwing exceptions.
     *
     * @param joinPoint the join point for the advice
     * @param e the exception thrown
     */
    @AfterThrowing(pointcut = "applicationPackagePointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        if (env.acceptsProfiles(Profiles.of(AppConstants.PROFILE_NOT_PROD))) {
            log.error(
                    "Exception in {}.{}() with cause = '{}' and exception = '{}'",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    e.getCause() == null ? "NULL" : e.getCause(),
                    e.getMessage(),
                    e);

        } else {
            log.error(
                    "Exception in {}.{}() with cause = {}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    e.getCause() == null ? "NULL" : e.getCause());
        }
    }

    /**
     * Advice that logs when a method is entered and exited.
     *
     * @param joinPoint the join point for the advice
     * @return the result of the method execution
     * @throws Throwable if the underlying method throws an exception
     */
    @Around("applicationPackagePointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        if (log.isTraceEnabled()) {
            log.trace(
                    "Enter: {}.{}()",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName());
        }
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long end = System.currentTimeMillis();
        if (log.isTraceEnabled()) {
            log.trace(
                    "Exit: {}.{}(). Time taken: {} millis",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    end - start);
        }
        return result;
    }
}
