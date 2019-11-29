package com.github.walterfan.potato.common.metrics;

import org.springframework.boot.logging.LogLevel;

public @interface LogDetail {
    LogLevel level() default LogLevel.INFO;
}
