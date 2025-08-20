package com.checkmate.bub.affirmation.controller;

import com.checkmate.bub.affirmation.dto.MainAffirmationResponseDto;
import com.checkmate.bub.affirmation.dto.ToneExampleRequestDto;
import com.checkmate.bub.affirmation.dto.ToneExampleResponseDto;
import com.checkmate.bub.affirmation.service.AffirmationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/v1/affirmations")
@RequiredArgsConstructor
@Slf4j
public class AffirmationController {

    private final AffirmationService affirmationService;

    @PostMapping("/tone-examples")
    public ResponseEntity<ToneExampleResponseDto> createToneExamples(
            @Valid @RequestBody ToneExampleRequestDto requestDto,
            Authentication authentication) {

        if (authentication == null || authentication.getName() == null) {
            log.error("인증 정보가 없습니다");
            throw new IllegalStateException("인증이 필요합니다");
        }
        
        Long userId = Long.parseLong(authentication.getName());
        log.info("Received request from userId: {}, problems={}, tone={}", userId, requestDto.getProblems(), requestDto.getTone());

        // 문제 선택 단계 처리
        if (requestDto.getProblems() != null && !requestDto.getProblems().isEmpty()) {
            if (requestDto.getProblems().size() > 3) {
                throw new IllegalArgumentException("최대 3개의 문제만 선택할 수 있습니다.");
            }
            ToneExampleResponseDto response = affirmationService.createToneExamples(requestDto.getProblems(), userId);
            return ResponseEntity.ok(response);
        }
        
        // 톤 선택 단계 처리
        if (requestDto.getTone() != null && !requestDto.getTone().trim().isEmpty()) {
            log.info("Tone selection received: {}", requestDto.getTone());
            affirmationService.saveToneSelection(userId, requestDto.getTone());
            ToneExampleResponseDto response = ToneExampleResponseDto.builder()
                    .tone1(requestDto.getTone())
                    .build();
            return ResponseEntity.ok(response);
        }

        throw new IllegalArgumentException("문제 ID 목록 또는 톤이 필요합니다.");
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
