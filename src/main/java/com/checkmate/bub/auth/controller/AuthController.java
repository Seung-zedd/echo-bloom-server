package com.checkmate.bub.auth.controller;

import com.checkmate.bub.auth.dto.AuthResponseDto;
import com.checkmate.bub.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/kakao")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 프론트엔드에서 받은 인가 코드로 로그인을 처리합니다.
     * @param code 카카오로부터 발급받은 1회용 인가 코드
     * @return 우리 서비스의 자체 JWT가 담긴 DTO
     */
    @GetMapping("/callback")
    public ResponseEntity<AuthResponseDto> kakaoCallback(@RequestParam("code") String code) {
        AuthResponseDto authResponse = authService.loginWithKakao(code);
        return ResponseEntity.ok(authResponse);
    }
}
