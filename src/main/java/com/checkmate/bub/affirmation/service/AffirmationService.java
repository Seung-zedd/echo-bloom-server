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

        // 응답 파싱 (3줄 가정)
        String[] tones = clovaResponse.split("\n");
        if (tones.length != 3) {
            throw new RuntimeException("Invalid Clova response format");
        }

        return ToneExampleResponseDto.builder()
                .tone1(tones[0])
                .tone2(tones[1])
                .tone3(tones[2])
                .build();

    }

    private String createPromptForToneExamples(String problemText) {
        return String.format(
                """
                        문제: "%s"
                        이 문제에 대해 3가지 다른 톤의 짧은 확언 문장을 생성해.
                        1줄: 진지한 톤 (20자 이내).
                        2줄: 친근한 톤 (20자 이내).
                        3줄: 유머러스한 톤 (20자 이내).
                        오직 3줄 문장만 출력.""", problemText);
    }
}
