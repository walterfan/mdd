package com.github.walterfan.potato.identity;

import com.github.walterfan.potato.common.domain.User;
import com.github.walterfan.potato.common.dto.ServiceHealth;
import com.github.walterfan.potato.common.dto.ServiceState;
import com.github.walterfan.potato.common.dto.TokenRequest;
import com.github.walterfan.potato.common.dto.TokenResponse;
import com.github.walterfan.potato.common.metrics.ApiCallMetricAnnotation;

import com.github.walterfan.potato.common.util.JwtUtil;
import com.github.walterfan.potato.common.util.NetworkUtil;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * @Author: Walter Fan
 **/
@RestController
@RequestMapping("/identity/api/v1")
@Slf4j
public class PotatoIdentityController {

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${server.port}")
    private Integer serverPort;

    @Autowired
    private TokenService tokenService;

    @RequestMapping(value = "/users", method = RequestMethod.POST)
    @ApiCallMetricAnnotation(name = "CreateUser")
    public User createUser(@RequestBody User potatoRequest) {
        log.info("create {}", potatoRequest);
        //TODO: change it
        return User.builder().email("walter.fan@gmail.com").build();

    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ApiCallMetricAnnotation(name = "CreateUser")
    public TokenResponse createToken(@RequestBody TokenRequest tokenRequest, HttpServletResponse response) {
        log.info("create {}", tokenRequest);

        TokenResponse tokenResponse =  tokenService.makeToken(tokenRequest);
        response.addHeader(AUTHORIZATION, "Bearer " + tokenResponse.getAccessToken());
        return tokenResponse;

    }

    @RequestMapping(value = "/ping", method = RequestMethod.GET)
    @ApiCallMetricAnnotation(name = "ping")
    public ServiceHealth ping() {
        return new ServiceHealth(this.serviceName, NetworkUtil.getLocalAddress()+ ":"+ serverPort, ServiceState.UP);

    }
}
