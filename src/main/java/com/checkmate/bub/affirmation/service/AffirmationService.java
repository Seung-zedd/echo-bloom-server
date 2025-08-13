package com.checkmate.bub.affirmation.service;

import com.checkmate.bub.affirmation.dto.ToneExampleRequestDto;
import com.checkmate.bub.affirmation.dto.ToneExampleResponseDto;
import com.checkmate.bub.ai.clova.ClovaClient;
import com.checkmate.bub.category.domain.Category;
import com.checkmate.bub.category.repository.CategoryRepository;
import com.checkmate.bub.util.UuidUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class AffirmationService {
    private final CategoryRepository categoryRepository;
    private final ClovaClient clovaClient;

    @Value("${clova.api-key}")
    private String apiKey;  // yml에서 주입

    public ToneExampleResponseDto createToneExamples(ToneExampleRequestDto requestDto) {
        Category problemCategory = categoryRepository.findById(requestDto.getProblemId())
                .orElseThrow(() -> new EntityNotFoundException("문제 카테고리를 찾을 수 없습니다."));

        String prompt = createPromptForToneExamples(problemCategory.getName());

        // 요청 바디 구성 (curl 기반)
        //todo: 하이퍼 파라미터 변경한다면 반영해서 코드 수정할 것
        Map<String, Object> body = new HashMap<>();
        body.put("messages", List.of(Map.of("role", "system", "content", prompt)));
        body.put("topP", 0.8);
        body.put("topK", 0);
        body.put("maxTokens", 80);
        body.put("temperature", 0.3);
        body.put("repetitionPenalty", 1.2);
        body.put("stop", List.of("\\n"));
        body.put("seed", 0);
        body.put("includeAiFilters", true);

        // UUID 생성 (여기서 동적으로 만듦)
        String requestId = UuidUtil.generateRequestId();

        // FeignClient 호출 (UUID 동적 생성)
        String clovaResponse = clovaClient.callApi(
                body,
                "Bearer " + apiKey,
                requestId,
                "application/json",
                "text/event-stream"
        );

        // 응답 검증 (문서의 실패 예시: code "50000" 등[1])
        if (clovaResponse.contains("\"code\": \"50000\"")) {
            log.error("Clova API Internal Server Error");
            throw new RuntimeException("Clova API 서버 오류");
        }

        // SSE 형식 처리: "data:" 줄만 추출하고 content 합치기
        StringBuilder contentBuilder = new StringBuilder();
        String[] lines = clovaResponse.split("\n");
        for (String line : lines) {
            if (line.startsWith("data:")) {
                String data = line.substring(5).trim();
                if (data.contains("\"content\":")) {
                    String token = data.split("\"content\":\"")[1].split("\"")[0];
                    contentBuilder.append(token);
                }
            }
        }

        String fullContent = contentBuilder.toString().trim().replaceAll("\\*\\*|#|:", "");  // 마크다운/콜론 제거
        String[] tones = fullContent.split("\\n");
        if (tones.length != 3) {
            log.error("Expected 3 tones: {}", fullContent);
            throw new RuntimeException("Invalid format");
        }

// DTO 빌드 (추가 정제: 만약 마크다운 남아 있으면 제거)
        return ToneExampleResponseDto.builder()
                .tone1(tones[0].trim())
                .tone2(tones[1].trim())
                .tone3(tones[2].trim())
                .build();

    }

    private String createPromptForToneExamples(String problemText) {
        return String.format(
                """
                        문제: "%s"
                    이 문제에 대해 3가지 다른 톤의 짧은 확언 문장을 생성해.
                    - 1줄: 진지한 톤 (20자 이내, 예: "스트레스를 이겨내라.").
                    - 2줄: 친근한 톤 (20자 이내, 예: "함께 이겨보자!").
                    - 3줄: 유머러스한 톤 (20자 이내, 예: "스트레스, 안녕!").
                    출력 형식: 정확히 3줄의 순수 텍스트 문장만. 마크다운(**, # 등), 이모지, 톤 이름, 추가 설명 없이 오직 문장만. 각 줄은 newline으로 구분. 예시처럼 마침표로 끝내기.""", problemText);
    }
}
