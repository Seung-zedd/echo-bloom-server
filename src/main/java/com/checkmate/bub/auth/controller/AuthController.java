package com.checkmate.bub.auth.controller;

import com.checkmate.bub.auth.dto.AuthResponseDto;
import com.checkmate.bub.auth.service.AuthService;
import com.checkmate.bub.util.EnvironmentUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Auth", description = "인증/인가 관련 API (카카오 소셜 로그인 및 인증 확인)")
@RestController
@RequestMapping("/auth/kakao")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final EnvironmentUtil envUtil;

    @Operation(summary = "카카오 소셜 로그인 콜백", description = "카카오 인가 코드를 받아 로그인 처리하고 JWT 토큰을 쿠키에 설정한 후 홈 페이지로 리다이렉트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "로그인 성공 및 리다이렉트"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 인가 코드"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 (e.g., 토큰 생성 실패)")
    })
    @GetMapping("/callback")
    public ResponseEntity<Void> kakaoCallback(@Parameter(name = "code", description = "카카오로부터 발급받은 1회용 인가 코드", required = true, example = "ABCDEFG...") @RequestParam("code") String code, HttpServletResponse response) {

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

    @Operation(summary = "인증 상태 확인", description = "현재 사용자의 JWT 토큰을 확인하여 인증 상태를 반환합니다. 쿠키가 포함된 요청으로 서버가 토큰을 검증합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증 성공 (Authenticated)"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 만료)"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/api/check-auth")
    public ResponseEntity<String> checkAuth() {
        return ResponseEntity.ok("Authenticated");
    }
}
