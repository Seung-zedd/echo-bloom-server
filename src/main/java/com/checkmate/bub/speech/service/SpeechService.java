package com.checkmate.bub.speech.service;

import com.checkmate.bub.ai.clova.ClovaSpeechClient;
import com.checkmate.bub.category.constant.CategoryType;
import com.checkmate.bub.speech.constant.DifferenceType;
import com.checkmate.bub.speech.dto.SpeechCompareResponseDto;
import com.checkmate.bub.speech.dto.SpeechRecognitionResponseDto;
import com.checkmate.bub.speech.entity.AffirmationLogEntity;
import com.checkmate.bub.speech.repository.AffirmationLogRepository;
import com.checkmate.bub.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * UR-USER-015~018, AR-ADMIN-002: 음성 인식 서비스
 * ClovaSpeechClient를 사용하여 실시간 음성 인식을 처리하고 정확도를 검증
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SpeechService {
    
    private final ClovaSpeechClient clovaSpeechClient;
    private final AffirmationLogRepository affirmationLogRepository;
    
    // application-common.yml에서 CLOVA_CLIENT_ID, CLOVA_CLIENT_SECRET 환경변수 주입
    @Value("${clova.speech-recognition.client-id}")
    private String clovaClientId;
    
    @Value("${clova.speech-recognition.client-secret}")
    private String clovaClientSecret;
    
    // 음성 인식 관련 상수들
    private static final int MAX_RETRY_COUNT = 3;  // 최대 재시도 횟수
    private static final double MIN_ACCURACY_THRESHOLD = 0.8; // 80% 정확도 기준
    private static final int TIMEOUT_SECONDS = 15; // 15초 타임아웃
    
    /**
     * UR-USER-015: 음성 인식 시작 - 오디오 데이터 처리
     * UR-USER-016: 음성 인식 성공 - 정확한 읽기 성공 시 응답 반환
     * UR-USER-017: 음성 인식 로그 저장 - 정확한 읽기 시 확언 로그 저장
     * UR-USER-018: 음성 인식 재시도 - 부정확하거나 15초 내 완료 실패 시 재시도 처리
     * AR-ADMIN-002: 음성 인식 동작 - 실시간 음성 인식 및 정확도 검증
     */
    public SpeechRecognitionResponseDto recognizeSpeech(MultipartFile audioFile, String originalSentence, 
                                                       String tone, Integer currentRetryCount) {
        try {
            String userNickname = SecurityUtils.getCurrentNickname();
            log.info("음성 인식 시작 - 사용자: {}, 재시도 횟수: {}", userNickname, currentRetryCount);
            
            // ClovaSpeechClient를 통한 STT 변환 (한국어 설정)
            Map<String, Object> sttResult = clovaSpeechClient.recognizeSpeech(
                    audioFile,
                    clovaClientId,
                    clovaClientSecret,
                    "multipart/form-data",
                    "application/json",
                    "ko-KR"
            );
            
            // STT 결과에서 텍스트 추출
            String recognizedText = extractTextFromSttResult(sttResult);
            log.info("STT 결과: {} -> {}", originalSentence, recognizedText);
            
            // 정확도 계산 (Levenshtein 거리 기반)
            double accuracy = calculateAccuracy(originalSentence, recognizedText);
            log.info("정확도: {}%", accuracy * 100);
            
            // UR-USER-016: 성공 기준 체크 (80% 이상)
            if (accuracy >= MIN_ACCURACY_THRESHOLD) {
                // UR-USER-017: 로그 저장 - 정확한 읽기 시 확언 로그 DB 저장
                saveAffirmationLog(originalSentence, tone);
                log.info("음성 인식 성공 - 로그 저장 완료");
                
                return SpeechRecognitionResponseDto.builder()
                        .success(true)
                        .needRetry(false)
                        .goHome(false) // UR-USER-023: 홈 화면 이동 플래그
                        .retryCount(currentRetryCount)
                        .maxRetryReached(false)
                        .recognizedText(recognizedText)
                        .accuracy(accuracy)
                        .logSaved(true)
                        .canBookmark(true) // UR-USER-021: 북마크 추가 가능
                        .build();
            } else {
                // UR-USER-018: 재시도 처리 - 부정확한 읽기 시
                int nextRetryCount = currentRetryCount + 1;
                boolean maxRetryReached = nextRetryCount >= MAX_RETRY_COUNT;
                
                log.warn("음성 인식 실패 - 정확도 부족. 재시도 {}/{}", nextRetryCount, MAX_RETRY_COUNT);
                
                return SpeechRecognitionResponseDto.builder()
                        .success(false)
                        .needRetry(!maxRetryReached)
                        .goHome(maxRetryReached) // UR-USER-019: 3회 실패 시 홈 버튼 표시
                        .retryCount(nextRetryCount)
                        .maxRetryReached(maxRetryReached)
                        .recognizedText(recognizedText)
                        .accuracy(accuracy)
                        .errorMessage(maxRetryReached ? "최대 재시도 횟수 초과" : "정확도가 낮습니다. 다시 시도해주세요.")
                        .errorCode(maxRetryReached ? "MAX_RETRY_EXCEEDED" : "LOW_ACCURACY")
                        .logSaved(false)
                        .canBookmark(false)
                        .build();
            }
            
        } catch (Exception e) {
            String userNickname = SecurityUtils.getCurrentNickname();
            log.error("음성 인식 중 오류 발생 - 사용자: {}", userNickname, e);
            return SpeechRecognitionResponseDto.builder()
                    .success(false)
                    .needRetry(currentRetryCount < MAX_RETRY_COUNT - 1)
                    .goHome(currentRetryCount >= MAX_RETRY_COUNT - 1)
                    .retryCount(currentRetryCount + 1)
                    .maxRetryReached(currentRetryCount >= MAX_RETRY_COUNT - 1)
                    .errorMessage("음성 인식 처리 중 오류가 발생했습니다.")
                    .errorCode("RECOGNITION_ERROR")
                    .logSaved(false)
                    .canBookmark(false)
                    .build();
        }
    }
    
    /**
     * POST /api/v1/speech/compare 엔드포인트용 상세 비교 분석
     * Levenshtein 편집 거리 알고리즘을 사용하여 상세한 차이점 분석 제공
     */
    public SpeechCompareResponseDto compareSpeech(MultipartFile audioFile, String originalSentence) {
        try {
            log.info("음성 상세 비교 시작 - 원본: {}", originalSentence);
            
            // STT 변환
            Map<String, Object> sttResult = clovaSpeechClient.recognizeSpeech(
                    audioFile,
                    clovaClientId,
                    clovaClientSecret,
                    "multipart/form-data",
                    "application/json",
                    "ko-KR"
            );
            
            // STT 결과에서 텍스트 추출
            String recognizedText = extractTextFromSttResult(sttResult);
            
            // Levenshtein 편집 거리 계산
            int editDistance = calculateLevenshteinDistance(originalSentence, recognizedText);
            double accuracy = calculateAccuracy(originalSentence, recognizedText);
            
            // 차이점 상세 분석 (단어 단위)
            List<SpeechCompareResponseDto.DifferenceDetail> differences = 
                    analyzeDifferences(originalSentence, recognizedText);
            
            log.info("상세 비교 완료 - 편집거리: {}, 정확도: {}%, 차이점: {}개", 
                    editDistance, accuracy * 100, differences.size());
            
            return SpeechCompareResponseDto.builder()
                    .originalSentence(originalSentence)
                    .recognizedText(recognizedText)
                    .editDistance(editDistance)
                    .accuracyPercentage(accuracy)
                    .differences(differences)
                    .build();
                    
        } catch (Exception e) {
            log.error("음성 비교 중 오류 발생", e);
            throw new RuntimeException("음성 비교 처리 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * UR-USER-017: 확언 로그 저장
     * 사용자가 문장을 정확히 읽었을 때 AffirmationLogEntity로 DB에 저장
     * userNickname: 현재 카카오 로그인된 사용자 닉네임 (Authentication에서 전달받음)
     * sentence: MainAffirmationResponseDto.affirmation1/2/3에서 가져온 문장
     * problem: CategoryType.PROBLEM으로 고정
     * createdAt: BaseTimeEntity에서 자동 설정 (읽은 시간)
     */
    private void saveAffirmationLog(String sentence, String tone) {
        String userNickname = SecurityUtils.getCurrentNickname();
        AffirmationLogEntity affirmationLog = AffirmationLogEntity.builder()
                .userNickname(userNickname) // 카카오 로그인 사용자 닉네임
                .sentence(sentence) // 확언 문장 내용
                .problem(CategoryType.PROBLEM) // 문제 카테고리 (enum)
                .tone(tone) // 톤 설정
                .build();
        
        affirmationLogRepository.save(affirmationLog);
        log.info("확언 로그 저장 완료 - 사용자: {}, 문장: {}", userNickname, sentence);
    }
    
    /**
     * 정확도 계산 (Levenshtein 거리 기반 유사도)
     * 편집 거리를 최대 길이로 나누어 0~1 사이의 정확도 반환
     */
    private double calculateAccuracy(String original, String recognized) {
        if (original == null || recognized == null) {
            return 0.0;
        }
        
        // 공백 제거 후 비교 (더 관대한 비교)
        String cleanOriginal = original.replaceAll("\\s+", "");
        String cleanRecognized = recognized.replaceAll("\\s+", "");
        
        int editDistance = calculateLevenshteinDistance(cleanOriginal, cleanRecognized);
        int maxLength = Math.max(cleanOriginal.length(), cleanRecognized.length());
        
        if (maxLength == 0) {
            return 1.0;
        }
        
        return Math.max(0.0, 1.0 - (double) editDistance / maxLength);
    }
    
    /**
     * Levenshtein 편집 거리 계산 알고리즘
     * 두 문자열 간의 최소 편집 횟수(삽입, 삭제, 치환)를 계산
     */
    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        // 초기화: 첫 번째 문자열을 빈 문자열로 만드는 비용
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        
        // 초기화: 두 번째 문자열을 빈 문자열로 만드는 비용
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        // 동적 프로그래밍으로 최소 편집 거리 계산
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1]; // 문자가 같으면 비용 없음
                } else {
                    dp[i][j] = 1 + Math.min(
                            dp[i - 1][j],     // 삭제
                            Math.min(
                                    dp[i][j - 1], // 삽입
                                    dp[i - 1][j - 1] // 치환
                            )
                    );
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    /**
     * 차이점 상세 분석 (단어 단위)
     * 원본과 인식된 텍스트의 차이점을 INSERT, DELETE, SUBSTITUTE로 분류
     */
    private List<SpeechCompareResponseDto.DifferenceDetail> analyzeDifferences(String original, String recognized) {
        List<SpeechCompareResponseDto.DifferenceDetail> differences = new ArrayList<>();
        
        // 단어 단위로 분할
        String[] originalWords = original.split("\\s+");
        String[] recognizedWords = recognized.split("\\s+");
        
        int maxLength = Math.max(originalWords.length, recognizedWords.length);
        
        for (int i = 0; i < maxLength; i++) {
            String origWord = i < originalWords.length ? originalWords[i] : null;
            String recogWord = i < recognizedWords.length ? recognizedWords[i] : null;
            
            if (origWord != null && recogWord != null) {
                // 두 단어가 모두 존재하지만 다름 - 치환
                if (!origWord.equals(recogWord)) {
                    differences.add(SpeechCompareResponseDto.DifferenceDetail.builder()
                            .type(DifferenceType.SUBSTITUTE)
                            .position(i)
                            .originalWord(origWord)
                            .recognizedWord(recogWord)
                            .build());
                }
            } else if (origWord != null) {
                // 원본에만 존재 - 삭제
                differences.add(SpeechCompareResponseDto.DifferenceDetail.builder()
                        .type(DifferenceType.DELETE)
                        .position(i)
                        .originalWord(origWord)
                        .recognizedWord("")
                        .build());
            } else if (recogWord != null) {
                // 인식된 것에만 존재 - 삽입
                differences.add(SpeechCompareResponseDto.DifferenceDetail.builder()
                        .type(DifferenceType.INSERT)
                        .position(i)
                        .originalWord("")
                        .recognizedWord(recogWord)
                        .build());
            }
        }
        
        return differences;
    }
    
    /**
     * 사용자의 음성 인식 기록 조회 (히스토리 및 캐릭터 성장용)
     * GET /api/v1/history/logs 엔드포인트에서 사용
     */
    @Transactional(readOnly = true)
    public List<AffirmationLogEntity> getUserSpeechLogs() {
        String userNickname = SecurityUtils.getCurrentNickname();
        return affirmationLogRepository.findByUserNicknameOrderByCreatedAtDesc(userNickname);
    }
    
    /**
     * CLOVA STT API 응답에서 인식된 텍스트 추출
     * 응답 형식: { "text": "인식된 텍스트" } 또는 다른 구조
     */
    private String extractTextFromSttResult(Map<String, Object> sttResult) {
        if (sttResult == null) {
            return "";
        }
        
        // CLOVA STT API 응답 구조에 따라 텍스트 추출
        // 일반적인 구조: { "text": "인식된 텍스트" }
        Object textObj = sttResult.get("text");
        if (textObj != null) {
            return textObj.toString().trim();
        }
        
        // 다른 가능한 필드명들 시도
        Object resultObj = sttResult.get("result");
        if (resultObj != null) {
            return resultObj.toString().trim();
        }
        
        // 중첩된 구조인 경우: { "results": [{ "text": "..." }] }
        Object resultsObj = sttResult.get("results");
        if (resultsObj instanceof List) {
            List<?> results = (List<?>) resultsObj;
            if (!results.isEmpty() && results.getFirst() instanceof Map) {
                Map<?, ?> firstResult = (Map<?, ?>) results.getFirst();
                Object nestedText = firstResult.get("text");
                if (nestedText != null) {
                    return nestedText.toString().trim();
                }
            }
        }
        
        log.warn("STT 결과에서 텍스트를 찾을 수 없습니다: {}", sttResult);
        return "";
    }
}