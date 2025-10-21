package com.checkmate.bub.global.util.helper;

import com.checkmate.bub.global.util.EnvironmentUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Cookie 관련 유틸리티 클래스
 * JWT 쿠키 생성 및 삭제를 담당
 */
@Component
@RequiredArgsConstructor
public class CookieHelper {

    private final EnvironmentUtil envUtil;

    /**
     * JWT 인증 쿠키 삭제 (로그아웃 및 회원 탈퇴 시 사용)
     * Access Token과 Refresh Token 쿠키를 모두 삭제합니다.
     *
     * @param response HttpServletResponse 객체
     */
    public void clearAuthCookies(HttpServletResponse response) {
        boolean cookieSecure = !envUtil.isHttpEnvironment();

        // Access Token 쿠키 삭제
        ResponseCookie expiredAccessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", expiredAccessCookie.toString());

        // Refresh Token 쿠키 삭제
        ResponseCookie expiredRefreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", expiredRefreshCookie.toString());
    }
}
