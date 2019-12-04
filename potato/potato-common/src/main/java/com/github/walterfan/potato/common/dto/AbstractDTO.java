package com.github.walterfan.potato.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.walterfan.potato.common.util.JsonUtil;


/**
 * @Author: Walter Fan
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
public class AbstractDTO {
    public AbstractDTO() {
    }

    public String toJson() {
        return JsonUtil.toJson(this);
    }
}
