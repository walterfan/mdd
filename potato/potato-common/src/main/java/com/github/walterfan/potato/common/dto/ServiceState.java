package com.github.walterfan.potato.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Strings;


public enum ServiceState {
    UP,
    DOWN,
    OUT_OF_SERVICE,
    WARN,
    UNKNOWN;

    @JsonValue
    public String toJson() {
        return this.name().toLowerCase();
    }

    @JsonCreator
    public static ServiceState fromJson(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return UNKNOWN;
        } else {
            try {
                return valueOf(value.toUpperCase());
            } catch (IllegalArgumentException var2) {
                return UNKNOWN;
            }
        }
    }
}
