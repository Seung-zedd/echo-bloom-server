package com.checkmate.bub.speech.controller;

import com.checkmate.bub.speech.dto.SpeechCompareResponseDto;
import com.checkmate.bub.speech.dto.SpeechRecognitionResponseDto;
import com.checkmate.bub.speech.domain.AffirmationLogEntity;
import com.checkmate.bub.speech.service.SpeechService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * UR-USER-015~018, AR-ADMIN-002: 음성 인식 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/speech")
@RequiredArgsConstructor
@Slf4j
public class SpeechController {
    
    private final SpeechService speechService;
    
    /**
     * UR-USER-015: 음성 인식 시작
     * UR-USER-016: 음성 인식 성공
     * UR-USER-017: 음성 인식 로그 저장
     * UR-USER-018: 음성 인식 재시도
     * AR-ADMIN-002: 음성 인식 동작
     */
    @PostMapping(value = "/recognize", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SpeechRecognitionResponseDto> recognizeSpeech(
            @RequestParam("audioFile") MultipartFile audioFile,
            @RequestParam("originalSentence") String originalSentence,
            @RequestParam("tone") String tone,
            @RequestParam(value = "retryCount", defaultValue = "0") Integer retryCount) {
        
        try {
            log.info("음성 인식 요청 - 원본 문장: {}, 재시도: {}", originalSentence, retryCount);
            
            // 오디오 파일 유효성 검증
            if (audioFile.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(SpeechRecognitionResponseDto.builder()
                                .success(false)
                                .errorMessage("오디오 파일이 비어있습니다.")
                                .errorCode("EMPTY_AUDIO_FILE")
                                .build());
            }
            
            // 음성 인식 처리
            SpeechRecognitionResponseDto response = speechService.recognizeSpeech(
                    audioFile, originalSentence, tone, retryCount);
            
            // HTTP 상태 코드 결정
            if (response.isSuccess()) {
                return ResponseEntity.ok(response); // 200 OK
            } else if (response.isMaxRetryReached()) {
                return ResponseEntity.badRequest().body(response); // 400 Bad Request
            } else {
                return ResponseEntity.ok(response); // 재시도 가능한 경우 200 OK
            }
            
        } catch (Exception e) {
            log.error("음성 인식 컨트롤러 오류", e);
            return ResponseEntity.status(504) // 504 Gateway Timeout
                    .body(SpeechRecognitionResponseDto.builder()
                            .success(false)
                            .errorMessage("서버 처리 시간 초과")
                            .errorCode("TIMEOUT")
                            .build());
        }
    }
    
    /**
     * POST /api/v1/speech/compare - 상세 음성 비교 분석
     * Levenshtein 편집 거리 알고리즘 사용
     */
    @PostMapping(value = "/compare", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SpeechCompareResponseDto> compareSpeech(
            @RequestParam("audioFile") MultipartFile audioFile,
            @RequestParam("originalSentence") String originalSentence) {
        
        try {
            log.info("음성 상세 비교 요청 - 원본: {}", originalSentence);
            
            if (audioFile.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            SpeechCompareResponseDto response = speechService.compareSpeech(audioFile, originalSentence);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("음성 비교 컨트롤러 오류", e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * GET /api/v1/speech/logs - 사용자 음성 인식 기록 조회
     * UR-USER-023~024: 히스토리 및 캐릭터 성장 지원
     */
    @GetMapping("/logs")
    public ResponseEntity<List<AffirmationLogEntity>> getUserSpeechLogs() {
        try {
            List<AffirmationLogEntity> logs = speechService.getUserSpeechLogs();
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("음성 로그 조회 오류", e);
            return ResponseEntity.status(500).build();
        }
    }
}