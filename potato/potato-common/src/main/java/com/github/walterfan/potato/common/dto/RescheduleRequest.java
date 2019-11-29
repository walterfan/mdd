package com.github.walterfan.potato.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author: Walter Fan
 * @Date: 22/6/2019, Sat
 **/
@Data
public class RescheduleRequest extends RemindEmailRequest {
    @NotBlank
    private String jobId;

}
