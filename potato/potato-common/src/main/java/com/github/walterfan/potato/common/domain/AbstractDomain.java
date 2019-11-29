package com.github.walterfan.potato.common.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.lang.Nullable;

import java.io.Serializable;

/**
 * @Author: Walter Fan
 * @Date: 9/6/2019, Sun
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AbstractDomain<PK extends Serializable> implements Serializable {
    private static final long serialVersionUID = -5554308939380869754L;
    @Nullable
    private PK id;
}
