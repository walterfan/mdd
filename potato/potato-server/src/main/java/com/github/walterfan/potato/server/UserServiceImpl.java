package com.github.walterfan.potato.server;



import com.github.walterfan.potato.common.domain.User;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @Author: Walter Fan
 * @Date: 9/6/2019, Sun
 **/
@Service
public class UserServiceImpl implements UserService {


    @Override
    public User getUser(UUID userId) {
        return null;
    }

    public static void main(String args[]) {

        String allowedAccessNumbers="0123";
        String str = "accessNumber";
        String ret = str.replaceAll("[^\\d]", "").trim();
        System.out.println("see" + ret + "--" + allowedAccessNumbers.contains(ret));

    }
}
