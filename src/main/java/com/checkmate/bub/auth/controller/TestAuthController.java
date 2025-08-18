package com.checkmate.bub.auth.controller;

import com.checkmate.bub.global.jwt.JwtTokenProvider;
import com.checkmate.bub.user.domain.User;
import com.checkmate.bub.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test/auth")
@RequiredArgsConstructor
@Slf4j
public class TestAuthController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/create-test-user")
    public ResponseEntity<Map<String, Object>> createTestUser() {
        
        User testUser = userRepository.findByKakaoId(999999L)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .kakaoId(999999L)
                            .nickname("테스트사용자")
                            .email("test@example.com")
                            .build();
                    return userRepository.save(newUser);
                });

        String accessToken = jwtTokenProvider.createAccessToken(testUser.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", testUser.getId());
        response.put("nickname", testUser.getNickname());
        response.put("accessToken", accessToken);
        
        log.info("Test user created: ID={}, KakaoID={}", testUser.getId(), testUser.getKakaoId());
        
        return ResponseEntity.ok(response);
    }
}