package com.github.walterfan.potato.server;



import com.github.walterfan.potato.common.domain.User;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @Author: Walter Fan
 **/
@Service
public class UserServiceImpl implements UserService {

    @Override
    public User getUser(UUID userId) {
        return null;
    }

}
