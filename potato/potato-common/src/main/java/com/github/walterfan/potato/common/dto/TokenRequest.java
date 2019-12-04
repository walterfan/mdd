package com.github.walterfan.potato.common.dto;

import lombok.Data;

import java.util.List;

/**
 * @Author: Walter Fan
 **/
@Data
public class TokenRequest extends AbstractDTO{
    private String grantType;
    private String subject;
    private String username;
    private String passwrod;
    private List<String> scopes;
}
