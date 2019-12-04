package com.github.walterfan.potato.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.github.walterfan.potato.common.dto.PotatoDTO.DATE_TIME_FMT;

/**
 * @Author: Walter Fan
 **/
@JsonPropertyOrder({"serviceName", "serviceType", "serviceState", "message", "serviceInstance", "lastUpdatedTime", "upstreamServices"})
@JsonIgnoreProperties(
        ignoreUnknown = true
)
@Getter
public class ServiceHealth extends AbstractDTO {
    @JsonProperty
    private final String serviceName;
    @JsonProperty
    private final String serviceUrl;
    @JsonProperty
    private final ServiceState serviceState;

    @Setter
    @JsonProperty
    private Boolean required;

    @Setter
    @JsonProperty
    private String message;

    @Setter
    @JsonProperty
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FMT)
    private Date lastUpdatedTime;

    @Setter
    @JsonProperty
    List<ServiceHealth> upstreamServices = new ArrayList<>();

    @JsonCreator
    public ServiceHealth(@JsonProperty("serviceName") String serviceName,
                         @JsonProperty("serviceUrl") String serviceUrl,
                         @JsonProperty("serviceState") ServiceState serviceState) {
        this.serviceName = serviceName;
        this.serviceUrl = serviceUrl;
        this.serviceState = serviceState;
        this.required = true;
        this.message = "";
        this.lastUpdatedTime = new Date();
    }
}
