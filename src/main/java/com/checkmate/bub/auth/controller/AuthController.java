package com.checkmate.bub.auth.controller;

import com.checkmate.bub.auth.dto.AuthResponseDto;
import com.checkmate.bub.auth.service.AuthService;
import com.checkmate.bub.util.EnvironmentUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Duration;

@RestController
@RequestMapping("/auth/kakao")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final EnvironmentUtil envUtil;

    @GetMapping("/callback")
    public ResponseEntity<Void> kakaoCallback(@RequestParam("code") String code, HttpServletResponse response) {

        log.info("Kakao callback invoked");
        AuthResponseDto authResponse = authService.loginWithKakao(code);

        // 유틸로 환경 체크
        boolean isLocal = envUtil.isLocalEnvironment();
        boolean cookieSecure = !isLocal;  // local: false, dev, prod: true

        // 액세스 토큰 쿠키 (ResponseCookie 사용)
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", authResponse.getAccessToken())
                .httpOnly(true)          // JS 접근 불가
                .secure(cookieSecure)    // local: false, dev, prod: true
                .sameSite("Strict")      // CSRF 방지 (여기서 설정 가능!)
                .path("/")               // 전체 경로
                .maxAge(Duration.ofSeconds(3600))  // 1시간
                .build();
        response.addHeader("Set-Cookie", accessCookie.toString());  // 헤더로 추가

        // 리프레시 토큰 쿠키 (유사)
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", authResponse.getRefreshToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofSeconds(604800))  // 7일
                .build();
        response.addHeader("Set-Cookie", refreshCookie.toString());

        // 신규 사용자인지 기존 사용자인지에 따라 다른 페이지로 리다이렉트
        String redirectPath = authResponse.isNewUser() ? "/views/search.html" : "/home.html";
        log.info("Redirecting to {} (isNewUser: {})", redirectPath, authResponse.isNewUser());
        
        URI redirectUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(redirectPath)
                .build()
                .toUri();
        return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
    }

    @GetMapping("/api/check-auth")
    public ResponseEntity<String> checkAuth() {
        return ResponseEntity.ok("Authenticated");
    }
}
