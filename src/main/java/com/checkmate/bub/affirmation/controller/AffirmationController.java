package com.checkmate.bub.affirmation.controller;

import com.checkmate.bub.affirmation.dto.MainAffirmationResponseDto;
import com.checkmate.bub.affirmation.dto.ToneExampleResponseDto;
import com.checkmate.bub.affirmation.service.AffirmationService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/v1/affirmations")
@RequiredArgsConstructor
@Slf4j
public class AffirmationController {

    private final AffirmationService affirmationService;

    @PostMapping("/tone-examples")
    public ResponseEntity<ToneExampleResponseDto> createToneExamples(
            @RequestBody @NotEmpty(message = "문제 ID 목록은 비어 있을 수 없습니다.") @Size(max = 3, message = "최대 3개의 문제만 선택할 수 있습니다.") List<Long> problemIds) {

        // 로그로 입력 확인
        log.info("Received problemIds: {}", problemIds);

        ToneExampleResponseDto response = affirmationService.createToneExamples(problemIds);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/main")
    public ResponseEntity<MainAffirmationResponseDto> getMainAffirmation(Authentication authentication) {
        
        if (authentication == null || authentication.getName() == null) {
            log.error("인증 정보가 없습니다");
            throw new IllegalStateException("인증이 필요합니다");
        }
        
        // JWT에서 사용자 ID 추출 (subject 클레임에서)
        String userId = authentication.getName(); // JWT의 subject 클레임에서 사용자 ID
        log.info("홈 화면 확언 문구 요청 - 사용자 ID: {}", userId);
        
        try {
            MainAffirmationResponseDto response = affirmationService.generateMainAffirmation(Long.parseLong(userId));
            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            log.error("유효하지 않은 사용자 ID 형식: {}", userId);
            throw new IllegalArgumentException("유효하지 않은 사용자 ID입니다");
        }
    }
}
