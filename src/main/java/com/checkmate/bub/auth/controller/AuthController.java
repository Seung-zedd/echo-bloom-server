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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Duration;

@RestController
@RequestMapping("/auth/kakao")
@RequiredArgsConstructor
@Slf4j
// BoilerPlate Class
public class AuthController {

    private final AuthService authService;
    private final EnvironmentUtil envUtil;

    @GetMapping("/callback")
    public ResponseEntity<Void> kakaoCallback(@RequestParam("code") String code, HttpServletResponse response) {

        log.info("Kakao callback invoked");
        AuthResponseDto authResponse = authService.loginWithKakao(code);

        // 유틸로 환경 체크 (HTTP 환경에서는 secure=false)
        boolean isHttpEnv = envUtil.isHttpEnvironment();
        boolean cookieSecure = !isHttpEnv;  // local/dev: false, prod: true

        // 액세스 토큰 쿠키 (ResponseCookie 사용)
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", authResponse.getAccessToken())
                .httpOnly(true)          // JS 접근 불가
                .secure(cookieSecure)    // local: false, dev, prod: true
                .sameSite("Lax")         // Strict -> Lax로 변경 (리다이렉트 시 쿠키 전달 허용)
                .path("/")               // 전체 경로
                .maxAge(Duration.ofSeconds(3600))  // 1시간
                .build();
        response.addHeader("Set-Cookie", accessCookie.toString());  // 헤더로 추가

        // 리프레시 토큰 쿠키 (유사)
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", authResponse.getRefreshToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")         // Strict -> Lax로 변경
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

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        // 쿠키 삭제를 위해 만료시간을 0으로 설정
        ResponseCookie expiredAccessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(!envUtil.isLocalEnvironment())
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", expiredAccessCookie.toString());
        
        ResponseCookie expiredRefreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(!envUtil.isLocalEnvironment())
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", expiredRefreshCookie.toString());
        
        return ResponseEntity.ok("Logout successful");
    }
    
    @GetMapping("/login-url")
    public ResponseEntity<String> getKakaoLoginUrl() {
        String kakaoLoginUrl = buildKakaoAuthUrl();
        return ResponseEntity.ok(kakaoLoginUrl);
    }
    
    private String buildKakaoAuthUrl() {
        String baseUrl = "https://kauth.kakao.com/oauth/authorize";
        // AuthService에서 이미 주입받은 설정값 사용
        String scope = "profile_nickname profile_image account_email";
        
        return String.format("%s?client_id=%s&redirect_uri=%s&response_type=code&scope=%s",
                baseUrl, 
                authService.getClientId(), 
                authService.getRedirectUri(), 
                scope);
    }
}
