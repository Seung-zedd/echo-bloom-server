package com.checkmate.bub.domain.speech.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AR-ADMIN-005: 커스텀 문장 생성/수정 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomSentenceRequestDto {
    
    // 커스텀 문장 내용
    private String sentence;
    
    // 문장의 톤
    private String tone;
}