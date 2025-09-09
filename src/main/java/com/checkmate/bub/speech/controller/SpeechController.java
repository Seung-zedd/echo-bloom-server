package com.checkmate.bub.speech.controller;

import com.checkmate.bub.speech.dto.SpeechRecognitionResponseDto;
import com.checkmate.bub.speech.service.SpeechService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


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
     * UR-USER-017(deprecated): 음성 인식 로그 저장
     * UR-USER-018: 음성 인식 재시도
     * AR-ADMIN-002: 음성 인식 동작
     */
    @PostMapping(value = "/recognize", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SpeechRecognitionResponseDto> recognizeSpeech(
            @RequestParam("audioFile") MultipartFile audioFile,
            @RequestParam("originalSentence") String originalSentence,
            @RequestParam(value = "retryCount", defaultValue = "0") Integer retryCount) {

        long startTime = System.currentTimeMillis();
        String requestId = java.util.UUID.randomUUID().toString().substring(0, 8);
        
        try {
            log.info("[STT-REQUEST-{}] 음성 인식 요청 시작 - 원본 문장: {}, 재시도: {}, 파일 크기: {}bytes", 
                    requestId, originalSentence, retryCount, audioFile.getSize());

            // 오디오 파일 유효성 검증
            if (audioFile.isEmpty()) {
                log.warn("[STT-REQUEST-{}] 요청 실패 - 빈 오디오 파일", requestId);
                return ResponseEntity.badRequest()
                        .body(SpeechRecognitionResponseDto.builder()
                                .success(false)
                                .errorMessage("오디오 파일이 비어있습니다.")
                                .errorCode("EMPTY_AUDIO_FILE")
                                .build());
            }
            // 파일 크기 검증 (예: 10MB 제한)
            if (audioFile.getSize() > 10 * 1024 * 1024) {
                log.warn("[STT-REQUEST-{}] 요청 실패 - 파일 크기 초과: {}bytes", requestId, audioFile.getSize());
                return ResponseEntity.badRequest()
                        .body(SpeechRecognitionResponseDto.builder()
                                .success(false)
                                .errorMessage("오디오 파일 크기가 10MB를 초과합니다.")
                                .errorCode("FILE_SIZE_EXCEEDED")
                                .build());
            }
            // 파일 형식 검증
            String contentType = audioFile.getContentType();
            if (contentType == null || !contentType.startsWith("audio/")) {
                log.warn("[STT-REQUEST-{}] 요청 실패 - 잘못된 파일 형식: {}", requestId, contentType);
                return ResponseEntity.badRequest()
                        .body(SpeechRecognitionResponseDto.builder()
                                .success(false)
                                .errorMessage("지원하지 않는 파일 형식입니다.")
                                .errorCode("INVALID_FILE_FORMAT")
                                .build());
            }

            // 음성 인식 처리
            log.info("[STT-REQUEST-{}] Clova Speech API 호출 시작", requestId);
            SpeechRecognitionResponseDto response = speechService.recognizeSpeech(
                    audioFile, originalSentence, retryCount);

            long processingTime = System.currentTimeMillis() - startTime;
            
            // HTTP 상태 코드 결정
            if (response.isSuccess()) {
                log.info("[STT-REQUEST-{}] 음성 인식 성공 - 처리 시간: {}ms, 정확도: {}%", 
                        requestId, processingTime, response.getAccuracy() != null ? String.format("%.1f", response.getAccuracy() * 100) : "N/A");
                return ResponseEntity.ok(response); // 200 OK
            } else if (response.isMaxRetryReached()) {
                log.warn("[STT-REQUEST-{}] 음성 인식 실패 - 최대 재시도 횟수 도달, 처리 시간: {}ms", 
                        requestId, processingTime);
                return ResponseEntity.badRequest().body(response); // 400 Bad Request
            } else {
                log.info("[STT-REQUEST-{}] 음성 인식 재시도 필요 - 처리 시간: {}ms, 현재 재시도: {}", 
                        requestId, processingTime, retryCount);
                return ResponseEntity.ok(response); // 재시도 가능한 경우 200 OK
            }

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("[STT-REQUEST-{}] 음성 인식 컨트롤러 오류 - 처리 시간: {}ms", requestId, processingTime, e);
            return ResponseEntity.status(504) // 504 Gateway Timeout
                    .body(SpeechRecognitionResponseDto.builder()
                            .success(false)
                            .errorMessage("서버 처리 시간 초과")
                            .errorCode("TIMEOUT")
                            .build());
        }
    }

    /**
     * //! 화면 설계를 다시 해야 하기 때문에 앱 런칭 이후에 사용할 예정
     * POST /api/v1/speech/compare - 상세 음성 비교 분석
     * Levenshtein 편집 거리 알고리즘 사용
     */
    /*@PostMapping(value = "/compare", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SpeechCompareResponseDto> compareSpeech(
            @RequestParam("audioFile") MultipartFile audioFile,
            @RequestParam("originalSentence") String originalSentence) {

        long startTime = System.currentTimeMillis();
        String requestId = java.util.UUID.randomUUID().toString().substring(0, 8);
        
        try {
            log.info("[STT-COMPARE-{}] 음성 상세 비교 요청 시작 - 원본: {}, 파일 크기: {}bytes", 
                    requestId, originalSentence, audioFile.getSize());

            if (audioFile.isEmpty()) {
                log.warn("[STT-COMPARE-{}] 요청 실패 - 빈 오디오 파일", requestId);
                return ResponseEntity.badRequest().build();
            }

            SpeechCompareResponseDto response = speechService.compareSpeech(audioFile, originalSentence);
            
            long processingTime = System.currentTimeMillis() - startTime;
            log.info("[STT-COMPARE-{}] 음성 비교 완료 - 처리 시간: {}ms, 정확도: {}%, 편집거리: {}", 
                    requestId, processingTime, 
                    response.getAccuracyPercentage() != null ? String.format("%.1f", response.getAccuracyPercentage() * 100) : "N/A",
                    response.getEditDistance());
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("[STT-COMPARE-{}] 음성 비교 컨트롤러 오류 - 처리 시간: {}ms", requestId, processingTime, e);
            return ResponseEntity.status(500).build();
        }
    }*/

    /**
     * GET /api/v1/speech/logs - 사용자 음성 인식 기록 조회
     * UR-USER-023~024(deprecated): 히스토리 및 캐릭터 성장 지원
     *//*
    @GetMapping("/logs")
    public ResponseEntity<List<AffirmationLogEntity>> getUserSpeechLogs() {
        try {
            List<AffirmationLogEntity> logs = speechService.getUserSpeechLogs();
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("음성 로그 조회 오류", e);
            return ResponseEntity.status(500).build();
        }
    }*/
}