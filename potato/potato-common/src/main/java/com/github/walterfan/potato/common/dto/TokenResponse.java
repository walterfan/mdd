package com.github.walterfan.potato.common.dto;

import lombok.Data;

import java.util.Date;

/**
 * @Author: Walter Fan
 **/
@Data
public class TokenResponse extends AbstractDTO {

    private String tokenType;
    private Date expiredTime;
    private String accessToken;
    private String refreshToken;
}
