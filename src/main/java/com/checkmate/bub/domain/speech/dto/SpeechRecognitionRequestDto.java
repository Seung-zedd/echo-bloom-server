package com.checkmate.bub.domain.speech.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * UR-USER-015: 음성 인식 시작 요청 DTO
 * 오디오 파일과 원본 문장을 받아서 음성 인식 처리
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpeechRecognitionRequestDto {
    
    // 음성 인식할 오디오 파일
    private MultipartFile audioFile;
    
    // 비교할 원본 문장 (MainAffirmationResponseDto.affirmation1/2/3)
    private String originalSentence;
    
    // 현재 재시도 횟수 (최대 3회)
    private Integer retryCount;
    
    // 문장의 톤
    private String tone;
}