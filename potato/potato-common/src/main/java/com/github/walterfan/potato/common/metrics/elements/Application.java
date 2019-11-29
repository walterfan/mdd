package com.github.walterfan.potato.common.metrics.elements;

import lombok.Data;

@Data
public class Application {

    private String service;
    private String component;
    private String version;

    public Application(String service, String component, String version) {
        this.service = service;
        this.component = component;
        this.version = version;
    }

}