package com.github.walterfan.potato.common.metrics.elements;

import lombok.Data;

@Data
public class Environment {

    private String name;
    private String address;

    public Environment(String name, String address) {
        this.name = name;
        this.address = address;
    }

}