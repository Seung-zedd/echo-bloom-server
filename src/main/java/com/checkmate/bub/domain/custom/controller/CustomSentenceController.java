package com.checkmate.bub.domain.custom.controller;

import com.checkmate.bub.domain.custom.domain.CustomSentence;
import com.checkmate.bub.domain.custom.dto.CustomSentenceRequestDto;
import com.checkmate.bub.domain.custom.dto.CustomSentenceResponseDto;
import com.checkmate.bub.domain.custom.service.CustomSentenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * UR-USER-028, UR-USER-036, UR-USER-037, UR-USER-038: 커스텀 문장 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/custom-sentences")
@RequiredArgsConstructor
@Slf4j
public class CustomSentenceController {

    private final CustomSentenceService customSentenceService;

    /**
     * UR-USER-036: 커스텀 문장 추가
     */
    @PostMapping
    public ResponseEntity<CustomSentenceResponseDto> addCustomSentence(@Valid @RequestBody CustomSentenceRequestDto request) {
        try {
            log.info("커스텀 문장 추가 요청 - 문장: {}", request.getSentence());

            CustomSentence customSentence = customSentenceService.addCustomSentence(request.getSentence());

            CustomSentenceResponseDto response = CustomSentenceResponseDto.of(
                    customSentence.getId(),
                    customSentence.getSentence(),
                    customSentence.getCreatedAt()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("커스텀 문장 추가 오류", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * UR-USER-037: 커스텀 문장 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<CustomSentenceResponseDto> updateCustomSentence(
            @PathVariable Long id,
            @Valid @RequestBody CustomSentenceRequestDto request) {
        try {
            log.info("커스텀 문장 수정 요청 - ID: {}, 문장: {}", id, request.getSentence());

            CustomSentence customSentence = customSentenceService.updateCustomSentence(id, request.getSentence());

            CustomSentenceResponseDto response = CustomSentenceResponseDto.of(
                    customSentence.getId(),
                    customSentence.getSentence(),
                    customSentence.getCreatedAt()
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("커스텀 문장 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("커스텀 문장 수정 오류", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * UR-USER-038: 커스텀 문장 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomSentence(@PathVariable Long id) {
        try {
            log.info("커스텀 문장 삭제 요청 - ID: {}", id);

            customSentenceService.deleteCustomSentence(id);
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            log.warn("커스텀 문장 삭제 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("커스텀 문장 삭제 오류", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * UR-USER-028: 사용자의 모든 커스텀 문장 조회
     */
    @GetMapping
    public ResponseEntity<List<CustomSentenceResponseDto>> getUserCustomSentences() {
        try {
            List<CustomSentenceResponseDto> customSentences = customSentenceService.getUserCustomSentences()
                    .stream()
                    .map(entity -> CustomSentenceResponseDto.of(
                            entity.getId(),
                            entity.getSentence(),
                            entity.getCreatedAt()
                    ))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(customSentences);
        } catch (Exception e) {
            log.error("커스텀 문장 조회 오류", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 특정 커스텀 문장 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomSentenceResponseDto> getCustomSentence(@PathVariable Long id) {
        try {
            CustomSentence customSentence = customSentenceService.getCustomSentence(id);

            CustomSentenceResponseDto response = CustomSentenceResponseDto.of(
                    customSentence.getId(),
                    customSentence.getSentence(),
                    customSentence.getCreatedAt()
            );

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("커스텀 문장 조회 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("커스텀 문장 조회 오류", e);
            return ResponseEntity.status(500).build();
        }
    }
}
