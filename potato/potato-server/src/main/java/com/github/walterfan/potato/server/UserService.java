package com.github.walterfan.potato.server;

import com.github.walterfan.potato.common.domain.User;

import org.springframework.stereotype.Service;

import java.util.UUID;


public interface UserService {
    User getUser(UUID userId);
}
