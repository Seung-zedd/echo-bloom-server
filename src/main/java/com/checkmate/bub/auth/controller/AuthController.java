package com.checkmate.bub.auth.controller;

import com.checkmate.bub.auth.dto.AuthResponseDto;
import com.checkmate.bub.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증/인가 관련 API")
@RestController
@RequestMapping("/auth/kakao")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "카카오 소셜 로그인", description = "카카오 인가 코드를 사용하여 로그인 처리 후 JWT를 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공 및 JWT 발급"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 인가 코드"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/callback")
    public ResponseEntity<AuthResponseDto> kakaoCallback(@Parameter(name = "code", description = "카카오로부터 발급받은 1회용 인가 코드", required = true, example = "ABCDEFG...") @RequestParam("code") String code) {
        AuthResponseDto authResponse = authService.loginWithKakao(code);
        return ResponseEntity.ok(authResponse);
    }
}
