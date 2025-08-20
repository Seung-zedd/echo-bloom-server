package com.checkmate.bub.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

// JWT 토큰을 생성하고, 검증하고, 정보를 추출하는 역할을 하는 핵심 클래스입니다.
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;

    // application.yml에 정의된 시크릿 키와 만료 시간을 주입받습니다.
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            @Value("${jwt.access-token-expiration-in-seconds}") long accessTokenExpiration,
                            @Value("${jwt.refresh-token-expiration-in-seconds}") long refreshTokenExpiration) {
        if (secretKey.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT secret key must be at least 256 bits");
        }
        if (accessTokenExpiration <= 0 || refreshTokenExpiration <= 0) {
            throw new IllegalArgumentException("Token expiration time must be positive");
        }

        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityInMilliseconds = accessTokenExpiration * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenExpiration * 1000;
    }

    /**
     * 사용자의 ID를 기반으로 Access Token을 생성합니다.
     * @param userId 우리 서비스의 User ID
     * @return 생성된 JWT 문자열
     */
    public String createAccessToken(Long userId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenValidityInMilliseconds);

        return Jwts.builder()
                .subject(userId.toString()) // 토큰의 주체로 사용자 ID를 저장
                .issuedAt(now) // 토큰 발급 시간
                .expiration(validity) // 토큰 만료 시간
                .signWith(key) // 1. signWith(key, algorithm) 대신 signWith(key) 사용
                .compact();
    }

    public String createRefreshToken(Long userId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenValidityInMilliseconds);

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(validity)
                .signWith(key) // 1. signWith(key, algorithm) 대신 signWith(key) 사용
                .compact();
    }

    /**
     * 주어진 JWT가 유효한지 검증합니다.
     * @param token 검증할 JWT 문자열
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            // 토큰이 만료되었거나, 서명이 잘못되었거나 등등...
            return false;
        }
    }

    /**
     * 유효한 JWT에서 사용자 정보를 추출하여 Spring Security의 Authentication 객체를 생성합니다.
     * @param token 유효한 JWT 문자열
     * @return Spring Security가 사용할 인증 정보
     */
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        String userId = claims.getSubject();

        //todo: 사용자 역할 정보 조회 로직 추가
        Collection<? extends GrantedAuthority> authorities = Collections.emptyList();

        // UserDetails 객체를 만들어 Authentication으로 반환합니다.
        // 이 UserDetails는 Spring Security가 내부적으로 사용자를 식별하는 데 사용됩니다.
        UserDetails principal = new User(userId, "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }
}
