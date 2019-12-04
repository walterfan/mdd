package com.github.walterfan.potato.common.dto;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

/**
 * @Author: Walter Fan
 **/
@Data
public class TomatoDTO extends AbstractDTO {
    private UUID potatoId;
    private UUID tomatoId;
    private Integer sn;
    private Date startTime;
    private Date endTime;
}

