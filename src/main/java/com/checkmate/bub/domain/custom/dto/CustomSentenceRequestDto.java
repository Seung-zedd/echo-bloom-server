package com.checkmate.bub.domain.custom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * UR-USER-036, UR-USER-037: 커스텀 문장 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomSentenceRequestDto {

    @NotBlank(message = "문장 내용은 필수입니다")
    @Size(max = 1000, message = "문장은 1000자 이내로 입력해주세요")
    private String sentence;
}
