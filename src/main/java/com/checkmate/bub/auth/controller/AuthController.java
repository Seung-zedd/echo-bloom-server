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

        log.info("Callback called with code: {}", code);
        AuthResponseDto authResponse = authService.loginWithKakao(code);

        // 유틸로 환경 체크
        boolean isDev = envUtil.isDevEnvironment();
        boolean cookieSecure = !isDev;  // dev: false, prod: true

        // 액세스 토큰 쿠키 (ResponseCookie 사용)
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", authResponse.getAccessToken())
                .httpOnly(true)          // JS 접근 불가
                .secure(cookieSecure)    // dev: false, prod: true
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

        // /home으로 리다이렉트
        //todo: 리다이렉트 엔드포인트가 수정되면 변경할 것
        log.info("Redirecting to /home.html");
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("http://localhost:8080/home.html"))
                .build();
    }

    @GetMapping("/api/check-auth")
    public ResponseEntity<String> checkAuth() {
        return ResponseEntity.ok("Authenticated");
    }
}
