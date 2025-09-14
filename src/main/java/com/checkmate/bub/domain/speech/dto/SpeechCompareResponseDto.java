package com.checkmate.bub.domain.speech.dto;

import com.checkmate.bub.domain.speech.constant.DifferenceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * POST /api/v1/speech/compare 엔드포인트용 응답 DTO
 * Levenshtein 편집 거리 알고리즘을 통한 상세 비교 결과
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpeechCompareResponseDto {
    
    // 원본 문장
    private String originalSentence;
    
    // STT로 변환된 텍스트
    private String recognizedText;
    
    // Levenshtein 편집 거리 값
    private Integer editDistance;
    
    // 정확도 퍼센티지
    private Double accuracyPercentage;
    
    // 차이점 상세 정보
    private List<DifferenceDetail> differences;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DifferenceDetail {
        // 차이 유형 enum 사용
        private DifferenceType type;
        
        // 위치
        private Integer position;
        
        // 원본 단어/문자
        private String originalWord;
        
        // 인식된 단어/문자
        private String recognizedWord;
    }
}