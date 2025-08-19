package com.checkmate.bub.speech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "문장은 필수입니다")
    @Size(max = 500, message = "문장은 500자 이하여야 합니다")
    private String sentence;

    
    // 문장의 톤
    @NotBlank(message = "톤은 필수입니다")
    private String tone;
}