package com.unconv.spring.web.advice;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.zalando.problem.spring.web.advice.ProblemHandling;

/** Global controller advice class responsible for handling unconverted exceptions. */
@ControllerAdvice
public class UnconvExceptionHandler implements ProblemHandling {}
