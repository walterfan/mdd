package com.github.walterfan.potato.common.metrics;


import com.github.walterfan.potato.common.dto.AbstractDTO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.boot.logging.LogLevel;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Aspect
public class LogAspect {

    private static final String TO_POTATO = "To Potato: [%s] %s";
    private static final String FROM_POTATO = "From Potato: [%s] %s";

    private final Map<LogLevel, Consumer<String>> logHandlers = new HashMap<>();

    public LogAspect() {
        logHandlers.put(LogLevel.ERROR, x -> log.error(x));
        logHandlers.put(LogLevel.WARN, x -> log.warn(x));
        logHandlers.put(LogLevel.INFO, x -> log.info(x));
        logHandlers.put(LogLevel.DEBUG, x -> log.debug(x));
        logHandlers.put(LogLevel.TRACE, x -> log.trace(x));
    }

    @Before("@annotation( logAnnotation ) ")
    public void logBefore(JoinPoint joinPoint, LogDetail logAnnotation) {
        String method = "";
        String path = "";
        Object requestBody = null;
        try {
            for (Object object : joinPoint.getArgs()) {
                if (object instanceof HttpServletRequest) {
                    method = ((HttpServletRequest) object).getMethod();
                    path = ((HttpServletRequest) object).getPathInfo();
                } else if (object instanceof AbstractDTO) {
                    requestBody = object;
                }
            }
            logWithLogLevel(TO_POTATO, method, path, requestBody, logAnnotation.level());
        } catch (Exception e) {
            log.error("Unable to log before method");
        }
    }


    @AfterReturning(
            pointcut = "execution(* com.github.walterfan.potato.controller.*.*(..)) && @annotation( logAnnotation )",
            returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, LogDetail logAnnotation, Object result) {
        String path = "";
        String status = "0";
        try {
            for (Object object : joinPoint.getArgs()) {
                if (object instanceof HttpServletRequest) {
                    path = ((HttpServletRequest) object).getPathInfo();
                } else if (object instanceof HttpServletResponse) {
                    status = String.valueOf(((HttpServletResponse) object).getStatus());
                }
            }
            logWithLogLevel(FROM_POTATO, status, path, result, logAnnotation.level());
        } catch (Exception e) {
            log.error("Unable to log after returning method");
        }
    }

    @AfterThrowing(
            pointcut = "execution(* com.github.walterfan.potato.controller.*.*(..)) && @annotation( logAnnotation )",
            throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, LogDetail logAnnotation, Throwable exception) {
        String path = "";
        Integer status = 0;
        try {
            for (Object object : joinPoint.getArgs()) {
                if (object instanceof HttpServletRequest) {
                    path = ((HttpServletRequest) object).getPathInfo();
                } else if (object instanceof HttpServletResponse) {
                    status = ((HttpServletResponse) object).getStatus();
                }
            }

            log.warn(FROM_POTATO, status, path);

        } catch (Exception e) {
            log.error("Unable to log after throwing method");
        }
    }

    private void logWithLogLevel(String logFormat, String method, String path, Object requestBody, LogLevel level) {

        String value = "";

        if (requestBody != null) {
            value = String.format(logFormat, method, path + " " + requestBody);
        } else {
            value = String.format(logFormat, method, path);
        }

        logHandlers.get(level).accept(value);
    }


}
