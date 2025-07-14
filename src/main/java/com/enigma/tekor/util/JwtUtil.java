package com.enigma.tekor.util;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.enigma.tekor.entity.User;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${jwt.expiration.access-token}")
    private long accessTokenExpiration;

    @Value("${jwt.expiration.refresh-token}")
    private long refreshTokenExpiration;
    
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    public String generateAccessToken(User user) {
        Algorithm algorithm = Algorithm.HMAC256(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        return JWT.create()
                .withIssuer(appName)
                .withSubject(user.getId().toString())
                .withExpiresAt(Instant.now().plusSeconds(accessTokenExpiration))
                .withIssuedAt(Instant.now())
                .withClaim("role", user.getRole().getName().replace("ROLE_", ""))
                .sign(algorithm);
    }

    public String generateRefreshToken(User user) {
        Algorithm algorithm = Algorithm.HMAC256(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return JWT.create()
                .withIssuer(appName)
                .withSubject(user.getId().toString())
                .withExpiresAt(Instant.now().plusSeconds(refreshTokenExpiration))
                .withIssuedAt(Instant.now())
                .withClaim("username", user.getUsername())
                .sign(algorithm);
    }

    public Map<String, String> getUserInfoByToken(String token) {
        try {
            JWTVerifier verifier = getVerifier();
            DecodedJWT decodedJWT = verifier.verify(token);

            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("userId", decodedJWT.getSubject());
            
            if (decodedJWT.getClaim("role") != null && !decodedJWT.getClaim("role").isNull()) {
                userInfo.put("role", decodedJWT.getClaim("role").asString());
            }
            
            return userInfo;
        } catch (JWTVerificationException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw e;
        }
    }

    private JWTVerifier getVerifier() {
        Algorithm algorithm = Algorithm.HMAC256(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return JWT.require(algorithm)
                .withIssuer(appName)
                .build();
    }

    public boolean validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret.getBytes(StandardCharsets.UTF_8));
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(appName).build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            JWTVerifier verifier = getVerifier();
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT.getClaim("username").asString();
        } catch (JWTVerificationException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw e;
        }
    }

    public String getSubjectFromToken(String token) {
        try {
            JWTVerifier verifier = getVerifier();
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT.getSubject();
        } catch (JWTVerificationException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw e;
        }
    }

    
}
