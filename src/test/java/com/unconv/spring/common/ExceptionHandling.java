package com.unconv.spring.common;

import static com.unconv.spring.consts.AppConstants.PROFILE_TEST;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.zalando.problem.spring.web.advice.ProblemHandling;

@Profile(PROFILE_TEST)
@ControllerAdvice
public final class ExceptionHandling implements ProblemHandling {}
