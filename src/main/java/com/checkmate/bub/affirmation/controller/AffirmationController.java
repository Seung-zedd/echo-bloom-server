package com.checkmate.bub.affirmation.controller;

import com.checkmate.bub.affirmation.dto.ToneExampleRequestDto;
import com.checkmate.bub.affirmation.dto.ToneExampleResponseDto;
import com.checkmate.bub.affirmation.service.AffirmationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/affirmations")
@RequiredArgsConstructor
public class AffirmationController {

    private final AffirmationService affirmationService;

    @PostMapping("/tone-examples")
    public ResponseEntity<ToneExampleResponseDto> createToneExamples(
            @Valid @RequestBody ToneExampleRequestDto requestDto) {

        ToneExampleResponseDto response = affirmationService.createToneExamples(requestDto);
        return ResponseEntity.ok(response);
    }
}
