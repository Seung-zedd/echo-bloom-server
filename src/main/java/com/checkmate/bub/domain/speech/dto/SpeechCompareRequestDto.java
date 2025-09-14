package com.checkmate.bub.domain.speech.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * POST /api/v1/speech/compare 엔드포인트용 요청 DTO
 * 오디오 파일과 원본 문장을 받아서 상세한 비교 분석 수행
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpeechCompareRequestDto {
    
    // 비교할 오디오 파일
    private MultipartFile audioFile;
    
    // 원본 문장
    private String originalSentence;
}