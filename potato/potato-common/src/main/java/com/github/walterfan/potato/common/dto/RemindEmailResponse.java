package com.github.walterfan.potato.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RemindEmailResponse extends AbstractDTO {
    private boolean success;
    private String jobId;
    private String jobGroup;
    private String message;

    public RemindEmailResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    @JsonCreator
    public RemindEmailResponse(@JsonProperty("success") boolean success,
                               @JsonProperty("jobId") String jobId,
                               @JsonProperty("jobGroup") String jobGroup,
                               @JsonProperty("message") String message) {
        this.success = success;
        this.jobId = jobId;
        this.jobGroup = jobGroup;
        this.message = message;
    }

}