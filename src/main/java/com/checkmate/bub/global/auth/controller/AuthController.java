package com.checkmate.bub.global.auth.controller;

import com.checkmate.bub.global.auth.dto.AuthResponseDto;
import com.checkmate.bub.global.auth.service.AuthService;
import com.checkmate.bub.global.util.EnvironmentUtil;
import com.checkmate.bub.global.util.helper.CookieHelper;
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
    private final CookieHelper cookieHelper;

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
        String redirectPath = authResponse.isNewUser() ? "/intro_start.html" : "/home.html";
        log.info("Redirecting to {} (isNewUser: {})", redirectPath, authResponse.isNewUser());
        
        URI redirectUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(redirectPath)
                .build()
                .toUri();
        return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        // 쿠키 삭제 (CookieHelper 사용)
        cookieHelper.clearAuthCookies(response);
        return ResponseEntity.ok("Logout successful");
    }
    
    @GetMapping("/login-url")
    public ResponseEntity<String> getKakaoLoginUrl() {
        String kakaoLoginUrl = buildKakaoAuthUrl();
        return ResponseEntity.ok(kakaoLoginUrl);
    }

    @GetMapping("/logout-url")
    public ResponseEntity<String> getKakaoLogoutUrl() {
        String kakaoLogoutUrl = buildKakaoLogoutUrl();
        return ResponseEntity.ok(kakaoLogoutUrl);
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

    private String buildKakaoLogoutUrl() {
        String baseUrl = "https://kauth.kakao.com/oauth/logout";

        // Extract base URL from existing redirect-uri configuration
        // e.g., "https://echobloom.co.kr/auth/kakao/callback" → "https://echobloom.co.kr/main.html"
        String redirectUri = authService.getRedirectUri();
        String logoutRedirectUri;

        // Parse the base URL (protocol + domain + port) from redirect-uri
        int callbackIndex = redirectUri.indexOf("/auth/kakao/callback");
        if (callbackIndex > 0) {
            String baseUri = redirectUri.substring(0, callbackIndex);
            logoutRedirectUri = baseUri + "/main.html";
        } else {
            // Fallback: use ServletUriComponentsBuilder
            logoutRedirectUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/main.html")
                    .build()
                    .toUriString();
        }

        return String.format("%s?client_id=%s&logout_redirect_uri=%s",
                baseUrl,
                authService.getClientId(),
                logoutRedirectUri);
    }
}
