package com.checkmate.bub.domain.speech.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AR-ADMIN-005: 커스텀 문장 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomSentenceResponseDto {
    
    // 문장 ID
    private Long id;
    
    // 커스텀 문장 내용
    private String sentence;
    
    // 문장의 톤
    private String tone;
    
    // 생성 시간
    private LocalDateTime createdAt;
    
    // 수정 시간
    private LocalDateTime updatedAt;
}