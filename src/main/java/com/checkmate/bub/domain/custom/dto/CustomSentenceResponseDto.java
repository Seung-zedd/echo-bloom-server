package com.checkmate.bub.domain.custom.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * UR-USER-028, UR-USER-036, UR-USER-037, UR-USER-038: 커스텀 문장 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomSentenceResponseDto {

    private Long id;
    private String sentence;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public static CustomSentenceResponseDto of(Long id, String sentence, LocalDateTime createdAt) {
        return CustomSentenceResponseDto.builder()
                .id(id)
                .sentence(sentence)
                .createdAt(createdAt)
                .build();
    }
}
