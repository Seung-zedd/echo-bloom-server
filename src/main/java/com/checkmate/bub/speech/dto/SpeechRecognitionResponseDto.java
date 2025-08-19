package com.checkmate.bub.speech.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * UR-USER-016, UR-USER-018, UR-USER-019: 음성 인식 결과 응답 DTO
 * 성공, 재시도, 실패 상황에 따른 응답 데이터
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpeechRecognitionResponseDto {
    
    // 인식 성공 여부
    private boolean success;
    
    // 재시도 필요 여부 (UR-USER-018)
    private boolean needRetry;
    
    // 홈 화면 이동 플래그 (UR-USER-019, UR-USER-023)
    private boolean goHome;
    
    // 현재 재시도 횟수
    private Integer retryCount;
    
    // 최대 재시도 횟수 도달 여부
    private boolean maxRetryReached;
    
    // 인식된 텍스트
    private String recognizedText;
    
    // 정확도 퍼센티지
    private Double accuracy;
    
    // 오류 메시지
    private String errorMessage;
    
    // 에러 코드
    private String errorCode;
    
    // UR-USER-024: 새로운 문장 3개 (성공 시)
    private List<String> newSentences;
    
    // 로그 저장 여부 (UR-USER-017)
    private boolean logSaved;
    
    // 북마크 추가 가능 여부 (UR-USER-021)
    private boolean canBookmark;
}