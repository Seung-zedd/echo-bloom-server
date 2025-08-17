package com.checkmate.bub.speech.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UR-USER-021, UR-USER-022: 북마크 추가/제거 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookmarkRequestDto {
    
    // 북마크할 문장 내용
    private String sentence;
    
    // 문장의 톤
    private String tone;
}