package com.github.walterfan.potato.identity;

import com.github.walterfan.potato.common.domain.Role;
import com.github.walterfan.potato.common.domain.Scope;
import com.github.walterfan.potato.common.domain.User;
import com.github.walterfan.potato.common.dto.TokenRequest;
import com.github.walterfan.potato.common.dto.TokenResponse;
import com.github.walterfan.potato.common.util.JwtUtil;
import com.google.common.collect.Sets;
import io.jsonwebtoken.io.Encoders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

/**
 * @Author: Walter Fan
 **/
@Service
public class TokenService {

    @Value("${api.credential}")
    private String credential;

    private User testUser;


    public TokenService() {
        testUser = new User();
        testUser.setOrgId(UUID.randomUUID());
        testUser.setId(UUID.randomUUID());
        testUser.setUserRoles(Sets.newHashSet(Arrays.asList(Role.GUEST)));
        testUser.setEmail("walter.fan@gmail.com");
        testUser.setName("Walter");
        testUser.setFullName("Walter Fan");
        testUser.setPassword("pass1234");
        testUser.setUserScopes(Sets.newHashSet(Arrays.asList(
                Scope.CREATE_POTATO,
                Scope.RETRIEVE_POTATO,
                Scope.UPDATE_POTATO,
                Scope.DELETE_POTATO)));
    }

    public TokenResponse makeToken(TokenRequest tokenRequest) {

        Optional<User> optUser = findUser(tokenRequest.getUsername(), tokenRequest.getPasswrod());
        if(!optUser.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User Not Found");
        }
        User user = optUser.get();

        Map<String, Object> claims = new HashMap<>();
        claims.put("orgId", user.getOrgId());
        claims.put("userId", user.getId());
        claims.put("roles", user.getUserRoles().stream().map(x -> x.name()).collect(toList()));
        claims.put("scopes",user.getUserScopes().stream().map(x -> x.name()).collect(toList()));

        String apiKey = Encoders.BASE64.encode(credential.getBytes());

        String accessToken = JwtUtil.createJws(tokenRequest.getSubject(), claims, 300, apiKey);
        String refreshToken = JwtUtil.createJws(tokenRequest.getSubject(), claims, 300, apiKey);


        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setTokenType("Bearer");
        tokenResponse.setExpiredTime(new Date(System.currentTimeMillis() + 300* 1000));
        tokenResponse.setRefreshToken(refreshToken);
        tokenResponse.setAccessToken(accessToken);

        return tokenResponse;
    }

    public Optional<User> findUser(String userName, String password) {

        if(testUser.getEmail().equals(userName) && testUser.getPassword().equals(password))
            return Optional.of(testUser);


        return Optional.empty();
    }
}
