package com.github.walterfan.potato.common.dto;

import lombok.Data;

@Data
public class SearchCriteria extends AbstractDTO {
    private final String key;
    private final String operation;
    private final Object value;

    public SearchCriteria(String key, String operation, Object value) {
        this.key = key;
        this.operation = operation;
        this.value = value;
    }
}
