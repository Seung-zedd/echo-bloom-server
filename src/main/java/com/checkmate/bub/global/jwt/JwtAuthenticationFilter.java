package com.checkmate.bub.global.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

// 모든 API 요청이 들어올 때마다 헤더의 JWT를 검사하는 필터입니다.
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 쿠키에서 토큰 추출 (기존 헤더 방식 + 쿠키 지원 추가)
        String token = resolveTokenFromCookieOrHeader(request);

        // 1. 헤더에서 토큰을 성공적으로 추출했고, 토큰이 유효하다면
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            // 2. 토큰에서 인증 정보를 추출합니다.
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            // 3. (가장 중요) SecurityContextHolder에 인증 정보를 저장합니다.
            // 이렇게 해야 컨트롤러나 서비스에서 @AuthenticationPrincipal 등으로 현재 사용자 정보를 가져올 수 있습니다.
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    // 쿠키 우선으로 토큰 추출 (없으면 헤더 확인)
    private String resolveTokenFromCookieOrHeader(HttpServletRequest request) {
        // 쿠키에서 "accessToken" 추출
        if (request.getCookies() != null) {
            String cookieToken = Arrays.stream(request.getCookies())
                    .filter(cookie -> "accessToken".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
            if (StringUtils.hasText(cookieToken)) {
                return cookieToken;
            }
        }

        // 쿠키 없으면 기존 헤더 방식 (Authorization: Bearer)
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
