package com.prudhvi.swacch.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    // Apply to all service classes
    @Around("execution(* com.prudhvi.swacch.service..*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {

        String methodName = joinPoint.getSignature().toShortString();

        log.info("➡️ Entering: {}", methodName);

        try {
            Object result = joinPoint.proceed();

            log.info("⬅️ Exiting: {}", methodName);

            return result;

        } catch (Exception e) {
            log.error("❌ Exception in: {}", methodName, e);
            throw e;
        }
    }
}
