package com.github.walterfan.potato.common.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.validator.constraints.Length;


import javax.validation.constraints.NotEmpty;
import java.util.HashSet;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User extends AbstractDomain<UUID> {

    @NotEmpty(message = "*Please provide an email")
    private String email;

    @Length(min = 5, message = "*Your password must have at least 5 characters")
    @NotEmpty(message = "*Please provide your password")
    private String password;

    @NotEmpty(message = "*Please provide your name")
    private String name;

    @NotEmpty(message = "*Please provide your full name")
    private String fullName;

    @NotEmpty(message = "*Please provide your orgId")
    private UUID orgId;

    private int active;

    private Boolean accountExpired = false;

    private Boolean credentialsExpired = false;

    private Boolean accountLocked = false;

    private Boolean enabled = true;

    private Set<Role> userRoles = new HashSet<>();

    private Set<Scope> userScopes = new HashSet<>();


}