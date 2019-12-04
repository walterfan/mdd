package com.github.walterfan.potato.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author: Walter Fan
 **/
@Data
public class RescheduleRequest extends RemindEmailRequest {
    @NotBlank
    private String jobId;

}
