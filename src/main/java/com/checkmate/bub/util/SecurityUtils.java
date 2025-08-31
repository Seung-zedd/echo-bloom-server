package com.checkmate.bub.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
// BoilerPlate Class
public class SecurityUtils {

    private SecurityUtils() {}  // 인스턴스화 방지

    public static String getCurrentNickname() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");  // 에러 핸들링 (필요 시)
        }
        return authentication.getName();  // JWT 클레임에서 nickname (카카오 실명) 반환
    }
}
