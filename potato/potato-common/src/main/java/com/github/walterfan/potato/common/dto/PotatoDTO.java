package com.github.walterfan.potato.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Walter Fan
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PotatoDTO extends AbstractDTO {
    public static final String DATE_TIME_FMT = "yyyy-MM-dd'T'HH:mm:ssXXX";
    private UUID id;
    @NotBlank
    private String name;

    private String description;

    private String tags;

    private Integer priority;

    private UUID userId;

    private String email;

    @NotNull
    private Long duration;

    @NotNull
    private TimeUnit timeUnit;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FMT)
    private ZonedDateTime deadline;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FMT)
    private ZonedDateTime scheduleTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FMT)
    private ZonedDateTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_FMT)
    private ZonedDateTime endTime;

}
