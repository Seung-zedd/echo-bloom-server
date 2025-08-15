package com.checkmate.bub.affirmation.controller;

import com.checkmate.bub.affirmation.dto.ToneExampleResponseDto;
import com.checkmate.bub.affirmation.service.AffirmationService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
