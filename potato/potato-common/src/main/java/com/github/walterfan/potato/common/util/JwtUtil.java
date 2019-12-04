package com.github.walterfan.potato.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @Author: Walter Fan
 **/
@Slf4j
public class JwtUtil {
    private static final Long ONE_HOUR_MS = 60 * 60 * 100L;

    public static String createJws(String subject, Map<String, Object> claims, long liveSeconds, String apiKey) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(apiKey);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());


        long expMillis = System.currentTimeMillis() + liveSeconds * 1000;
        Date expireDate = new Date(expMillis);

        JwtBuilder jwtBuilder = Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(signingKey);

        if(!Collections.isEmpty(claims)) {
            claims.entrySet().stream().forEach( x -> jwtBuilder.claim(x.getKey(), x.getValue()));
        }

        return jwtBuilder.compact();
    }


    public static Claims parseJws(String compactJws, String apiKey) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(apiKey);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        Claims ret = Jwts.parser()
                .setSigningKey(signingKey)
                .parseClaimsJws(compactJws)
                .getBody();
        return ret;

    }


    public static void main(String[] args) {
        String proverb = "A journey of a thousand miles begins with a single step";
        String apiKey = Encoders.BASE64.encode(proverb.getBytes());
        Map<String, Object> map = new HashMap<>();
        map.put("orgId", UUID.randomUUID());
        map.put("userId", UUID.randomUUID());
        map.put("roles", Arrays.asList("admin"));
        map.put("scopes", Arrays.asList("read", "write"));


        String jws= createJws("potato", map, 300, apiKey);

        log.info("jws = {}", jws);

        Claims claims =  parseJws(jws, apiKey);
        claims.entrySet().stream().forEach(x ->  log.info("{} = {}", x.getKey(), x.getValue()));


    }
}

