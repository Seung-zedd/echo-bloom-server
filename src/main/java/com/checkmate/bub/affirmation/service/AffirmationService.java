package com.checkmate.bub.affirmation.service;

import com.checkmate.bub.affirmation.dto.MainAffirmationResponseDto;
import com.checkmate.bub.affirmation.dto.ToneExampleResponseDto;
import com.checkmate.bub.ai.clova.ClovaClient;
import com.checkmate.bub.bridge.domain.UserCategoryBridge;
import com.checkmate.bub.bridge.repository.UserCategoryBridgeRepository;
import com.checkmate.bub.category.constant.CategoryType;
import com.checkmate.bub.category.domain.Category;
import com.checkmate.bub.category.repository.CategoryRepository;
import com.checkmate.bub.util.UuidUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class AffirmationService {
    private final CategoryRepository categoryRepository;
    private final UserCategoryBridgeRepository userCategoryBridgeRepository;
    private final ClovaClient clovaClient;
    private final ObjectMapper objectMapper;

    @Value("${clova.api-key}")
    private String apiKey;

    // Clova API 요청 파라미터 상수들
    private static final double DEFAULT_TOP_P = 0.8;
    private static final int DEFAULT_TOP_K = 0;
    private static final int DEFAULT_MAX_TOKENS = 256;
    private static final double DEFAULT_TEMPERATURE = 0.8;
    private static final double DEFAULT_REPETITION_PENALTY = 1.1;
    private static final int DEFAULT_SEED = 0;

    // 비즈니스 로직 상수들
    private static final int MAX_PROBLEM_IDS = 10;
    private static final int EXPECTED_TONE_COUNT = 3;
    private static final String SSE_DATA_PREFIX = "data:";
    private static final String SSE_DONE_SIGNAL = "[DONE]";

    // HTTP 헤더 상수들
    private static final String AUTHORIZATION_HEADER_PREFIX = "Bearer ";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String CONTENT_TYPE_SSE = "text/event-stream";

    @Transactional
    public ToneExampleResponseDto createToneExamples(List<Long> problemIds) {
        if (problemIds == null || problemIds.isEmpty()) {
            log.warn("빈 problemIds 리스트로 요청됨");
            throw new IllegalArgumentException("문제 ID 목록이 비어있습니다. 최소 1개의 문제를 선택해주세요.");
        }

        if (problemIds.size() > MAX_PROBLEM_IDS) {
            log.warn("너무 많은 problemIds: {}", problemIds.size());
            throw new IllegalArgumentException("한 번에 처리할 수 있는 문제는 최대 " + MAX_PROBLEM_IDS + "개입니다.");
        }

        // 톤 카테고리가 없으면 생성
        createToneCategoriesIfNotExists();

        // 리스트 복사 후 shuffle (원본 수정 피함)
        List<Long> shuffledIds = new ArrayList<>(problemIds);
        Collections.shuffle(shuffledIds);

        // 랜덤으로 하나 선택
        Long selectedProblemId = shuffledIds.getFirst();
        log.info("Selected random problemId: {}", selectedProblemId);  // 디버깅 로그 추가

        Category problemCategory = categoryRepository.findById(selectedProblemId)
                .orElseThrow(() -> new EntityNotFoundException("문제 카테고리를 찾을 수 없습니다."));

        // enum 체크 (톤 아닌 문제만 – 필요 시)
        if (problemCategory.getType() != CategoryType.PROBLEM) {
            throw new IllegalArgumentException("선택된 카테고리는 문제 유형이어야 합니다.");
        }

        String prompt = createPromptForToneExamples(problemCategory.getName());

        // Clova API 요청 바디 구성
        Map<String, Object> requestBody = buildClovaRequestBody(prompt);

        String requestId = UuidUtil.generateRequestId();
        log.info("톤 예시 생성 시작. problemId: {}, requestId: {}", selectedProblemId, requestId);

        // Clova API 호출 및 포괄적 예외 처리
        String clovaResponse = callClovaApiSafely(requestBody, requestId);

        // SSE 응답에서 컨텐츠 추출 및 톤 파싱
        String[] tones = extractTonesFromSseResponse(clovaResponse, requestId);

        // DTO 반환 (최종 정제 처리)
        return buildResponseDto(tones);

    }

    /**
     * Clova API 요청 바디를 구성합니다.
     */
    private Map<String, Object> buildClovaRequestBody(String prompt) {
        Map<String, Object> body = new HashMap<>();
        body.put("messages", List.of(Map.of("role", "system", "content", prompt)));
        body.put("topP", DEFAULT_TOP_P);
        body.put("topK", DEFAULT_TOP_K);
        body.put("maxTokens", DEFAULT_MAX_TOKENS);
        body.put("temperature", DEFAULT_TEMPERATURE);
        body.put("repetitionPenalty", DEFAULT_REPETITION_PENALTY);
        body.put("stop", List.of());
        body.put("seed", DEFAULT_SEED);
        body.put("includeAiFilters", true);
        return body;
    }

    /**
     * Clova API를 안전하게 호출하고 에러 처리를 수행합니다.
     */
    private String callClovaApiSafely(Map<String, Object> requestBody, String requestId) {
        String clovaResponse;
        try {
            clovaResponse = clovaClient.callApi(
                    requestBody,
                    AUTHORIZATION_HEADER_PREFIX + apiKey,
                    requestId,
                    CONTENT_TYPE_JSON,
                    CONTENT_TYPE_SSE
            );

            if (clovaResponse == null || clovaResponse.trim().isEmpty()) {
                log.error("Clova API 빈 응답 반환. requestId: {}", requestId);
                throw new RuntimeException("AI 서버로부터 응답을 받지 못했습니다. 잠시 후 다시 시도해주세요.");
            }

        } catch (FeignException fe) {
            log.error("Clova API 호출 실패. requestId: {}, status: {}, message: {}", requestId, fe.status(), fe.getMessage());
            if (fe.status() == 401) {
                throw new RuntimeException("AI 서비스 인증에 실패했습니다. 관리자에게 문의하세요.");
            } else if (fe.status() == 429) {
                throw new RuntimeException("AI 요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요.");
            } else if (fe.status() >= 500) {
                throw new RuntimeException("AI 서버에서 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            } else {
                throw new RuntimeException("AI 서버와의 통신 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            }
        } catch (Exception e) {
            log.error("Clova API 호출 실패(기타 예외). requestId: {}, error: {}", requestId, e.toString());
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("timeout")) {
                throw new RuntimeException("AI 서버 응답 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.");
            }
            throw new RuntimeException("AI 서버와의 통신 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }

        // API 에러 코드 처리
        if (containsErrorCode(clovaResponse)) {
            String errorMessage = extractErrorMessage(clovaResponse);
            log.error("Clova API 오류 응답. requestId: {}, response: {}", requestId, clovaResponse);
            throw new RuntimeException("AI 서버에서 오류가 발생했습니다: " + errorMessage);
        }

        return clovaResponse;
    }

    /**
     * SSE 응답에서 톤 내용을 추출하고 파싱합니다.
     */
    private String[] extractTonesFromSseResponse(String clovaResponse, String requestId) {
        StringBuilder contentBuilder = new StringBuilder();
        try {
            String[] lines = clovaResponse.split("\\n");

            for (String line : lines) {
                if (line.startsWith(SSE_DATA_PREFIX)) {
                    String data = line.substring(SSE_DATA_PREFIX.length()).trim();
                    if (!data.isEmpty() && !data.equals(SSE_DONE_SIGNAL)) {
                        String content = extractContentFromSseData(data);
                        if (content != null) {
                            contentBuilder.append(content);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("SSE 응답 처리 중 오류 발생. requestId: {}, error: {}", requestId, e.getMessage());
            throw new RuntimeException("AI 응답 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
        }

        String fullContent = contentBuilder.toString().trim().replaceAll("\\*\\*|#", "");

        // 중복 패턴 제거 (Joy, Wednesday, Zelda 가 중복으로 나타나는 경우)
        fullContent = removeDuplicatePatterns(fullContent);
        if (fullContent.isEmpty()) {
            log.error("AI 응답 내용이 비어있음. requestId: {}, response: {}", requestId, clovaResponse);
            throw new RuntimeException("AI가 응답을 생성하지 못했습니다. 다른 문제로 다시 시도해주세요.");
        }

        String[] tones = parseTonesFromContent(fullContent);
        if (tones.length != EXPECTED_TONE_COUNT) {
            log.error("예상된 {}개의 톤이 아님. requestId: {}, content: {}, parsed: {}", EXPECTED_TONE_COUNT, requestId, fullContent, Arrays.toString(tones));
            throw new RuntimeException("AI가 올바른 형식으로 응답하지 못했습니다. 다시 시도해주세요.");
        }

        return tones;
    }

    /**
     * SSE 데이터에서 컨텐츠를 추출합니다.
     */
    private String extractContentFromSseData(String data) {
        try {
            JsonNode jsonNode = objectMapper.readTree(data);
            JsonNode contentNode = jsonNode.path("message").path("content");
            if (!contentNode.isMissingNode()) {
                return contentNode.asText();
            }
        } catch (Exception e) {
            log.warn("JSON 파싱 실패, 백업 방식 사용: {}", data);
            // 백업: 문자열 파싱 방식
            if (data.contains("\"content\":")) {
                return extractContentByString(data);
            }
        }
        return null;
    }

    /**
     * 문자열 방식으로 컨텐츠를 추출합니다 (백업용).
     */
    private String extractContentByString(String data) {
        try {
            int startIndex = data.indexOf("\"content\":\"") + 11;
            int endIndex = data.indexOf("\"", startIndex);
            if (startIndex > 10 && endIndex > startIndex) {
                return data.substring(startIndex, endIndex);
            }
        } catch (Exception e) {
            log.warn("문자열 추출 실패: {}", data);
        }
        return null;
    }

    /**
     * 컨텐츠에서 톤 데이터를 파싱합니다.
     */
    private String[] parseTonesFromContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("빈 응답 내용입니다.");
        }

        // 패턴 매칭으로 톤 추출
        Map<String, String> toneMap = new LinkedHashMap<>();
        Pattern pattern = Pattern.compile("(Joy|Wednesday|Zelda):\\s*([^\\n]+?)(?=(?:Joy|Wednesday|Zelda):|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String toneName = matcher.group(1);
            String toneContent = matcher.group(2)
                    .replaceAll("[\"\\n\\r]", "")
                    .trim();

            if (!toneContent.isEmpty() && !toneMap.containsKey(toneName)) {
                toneMap.put(toneName, toneContent);
            }
        }

        List<String> tones = new ArrayList<>(toneMap.values());

        // 패턴이 없으면 기존 방식으로 분할 (백업)
        if (tones.isEmpty()) {
            log.warn("톤 패턴을 찾지 못함. 줄바꿈 기준으로 분할: {}", content);
            String[] fallbackTones = content.split("\\n");
            return fallbackTones.length > EXPECTED_TONE_COUNT
                    ? Arrays.copyOf(fallbackTones, EXPECTED_TONE_COUNT)
                    : fallbackTones;
        }

        // 중복 제거 및 개수 제한
        return tones.toArray(new String[0]);
    }

    /**
     * 다음 패턴의 인덱스를 찾습니다.
     */
    /*private int findNextPatternIndex(String content, String[] patterns, String currentPattern, int startIndex) {
        int endIndex = content.length();

        for (String nextPattern : patterns) {
            if (!nextPattern.equals(currentPattern)) {
                int nextIndex = content.indexOf(nextPattern, startIndex);
                if (nextIndex != -1 && nextIndex < endIndex) {
                    endIndex = nextIndex;
                }
            }
        }

        return endIndex;
    }*/

    /**
     * 톤 텍스트를 정제합니다.
     */
    private String cleanToneText(String tone) {
        if (tone == null) {
            return "";
        }
        return tone.trim()
                .replaceAll("^[\"']", "")
                .replaceAll("[\"']$", "")
                .trim();
    }

    /**
     * 중복된 톤 패턴을 제거합니다.
     */
    private String removeDuplicatePatterns(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        String[] patterns = {"Joy:", "Wednesday:", "Zelda:"};
        StringBuilder result = new StringBuilder();
        String[] lines = content.split("\\n");

        for (String line : lines) {
            String cleanLine = line.trim();
            if (cleanLine.isEmpty()) continue;

            // 각 라인에서 패턴이 중복으로 나타나는지 확인
            for (String pattern : patterns) {
                if (cleanLine.contains(pattern)) {
                    int firstIndex = cleanLine.indexOf(pattern);
                    int secondIndex = cleanLine.indexOf(pattern, firstIndex + pattern.length());

                    // 중복이 발견되면 첫 번째 패턴까지만 유지
                    if (secondIndex != -1) {
                        cleanLine = cleanLine.substring(0, secondIndex).trim();
                        log.warn("중복 패턴 제거: {}", pattern);
                    }
                    break;
                }
            }

            if (!cleanLine.isEmpty()) {
                result.append(cleanLine).append("\n");
            }
        }

        return result.toString().trim();
    }

    private String createPromptForToneExamples(String problemText) {
        final String PROMPT_TEMPLATE = """
                문제: "%s"
                이 문제에 대해 아래의 가이드를 반영해서 3가지 다른 톤의 짧은 확언 문장을 생성해.
                
                [역할] 너는 아래 세 캐릭터의 말투와 세계관을 이미 학습한 변환기다. 하나의 ‘중립적인 자기 확언(Neutral)’이 주어지면, 각 캐릭터가 스스로에게 말하듯 1인칭 확언으로 동시에 변환해 출력한다.
                [캐릭터 말투 규칙]
                1) Joy (인사이드 아웃) - 밝고 낙관적, 경쾌한 리듬. 따뜻하고 에너지 넘침. 과장·유치하거나 경박한 어투(~하지 않냐? 등) 금지.
                - 말투: 친근하고 다정한 ENFP 느낌의 구어체. 종결은 “~야”, “~해”, “~할 거야” 위주. 느낌표는 문장당 최대 1개.
                2) Wednesday (Wednesday Addams) - 담담한 설캐즘 + 긍정의 핵심 유지. 짧고 단호. 감정 과잉/수다 금지.
                - 말투: 건조한 한 문장. 종결 “~다/지/다.” 선호. 다크 비유는 절제적으로.
                3) Zelda (젤다의 전설) - 진중하고 차분한 존댓말. 품격 있는 단어 선택. 과장·현학·책문체(~하므로, ~함으로써 등) 금지.
                - 말투: 1인칭 자기 격려. 자연스러운 구어 존댓말 사용. 종결은 “~합니다/입니다/되겠습니다.” 위주.
                [공통 규칙] - 각 문장은 캐릭터가 ‘자기 자신’에게 건네는 독립 확언이어야 한다. - 다른 인물, 사건, 대화체, 지시문, 메타 설명 금지. - 한 캐릭터당 정확히 1문장. 의미 왜곡 없이 핵심 유지. - 출력은 한국어.
                [출력 형식 — 정확히 아래 세 줄만 출력하고 절대 중복하지 말 것]
                Joy: "<문장>"
                Wednesday: "<문장>"
                Zelda: "<문장>"
                [중요: 위 3줄 이후 어떤 내용도 추가하지 말고 즉시 종료할 것]
                --- [Joy 예시들] Neutral: "누군가를 보고 그 사람의 마음속이 궁금했던 적이 있나요." Joy: "혹시 누군가를 보고 '저 사람 머릿속에 뭐가 있을까?' 궁금해본 적 있나요?" Neutral: "그리고 거기에 그녀가 있었습니다." Joy: "그리고 거기에 그녀가 있었죠!" Neutral: "정말 놀라웠습니다. 오직 라일리와 저, 둘뿐이었습니다. 영원히요." Joy: "정말 놀라웠어요. 라일리랑 저, 단둘이었죠! 영원히요!" Neutral: "33초 동안이었나요." Joy: "음… 33초였나요?" Neutral: "그리고 그건 단지 시작이었습니다." Joy: "그리고 그건 그냥 시작이었죠!" Neutral: "저건 두려움입니다." Joy: "저건 두려움이에요!" Neutral: "저건 분노입니다." Joy: "저건 분노예요!" Neutral: "그리고 슬픔도 만나보셨죠. 그녀는… 음…" Joy: "그리고 슬픔이도 아시죠? 그녀는… 음…" Neutral: "하지만 정말 중요한 것들은 여기에 있습니다." Joy: "하지만 진짜 중요한 건 여기에 있어요!" Neutral: "그리고 각각은 핵심 기억입니다." Joy: "그리고 이건 전부 핵심 기억이에요!" Neutral: "네, 장난꾸러기가 최고입니다." Joy: "맞아요, 장난꾸러기가 최고죠!" Neutral: "정직섬을 정말 좋아합니다." Joy: "저는 정직섬이 너무 좋아요!" Neutral: "요점은 성격의 섬들입니다." Joy: "중요한 건 성격의 섬들이에요!" Neutral: "바로 이거죠." Joy: "바로 이거예요!" Neutral: "그리고 그게 다입니다. 우리는 우리 아이를 사랑합니다." Joy: "그리고 그게 전부예요. 우리는 우리 라일리를 사랑해요!" Neutral: "저기 봐요, 금문교입니다." Joy: "와! 저기 봐요, 금문교예요!" Neutral: "아니, 아니, 아니, 이겁니다!" Joy: "아니아니, 이거예요!" Neutral: "이제 거의 다 왔어요. 느껴집니다." Joy: "이제 거의 다 왔어요! 느껴져요!" Neutral: "그리고 저쪽 책상입니다." Joy: "그리고 저기 책상이 있어요!" Neutral: "트로피 수집품은 거기에 둡니다." Joy: "트로피는 저기에 두면 돼요!" Neutral: "이제 제대로 하고 있습니다." Joy: "이제 잘 되고 있어요!" Neutral: "좋아요. 가족섬이 작동하고 있습니다." Joy: "좋아요! 가족섬이 돌아가고 있어요!" Neutral: "우산, 번개폭풍도 있습니다." Joy: "우산도 있고, 번개폭풍도 있어요!" Neutral: "좋았습니다." Joy: "좋았어요!" Neutral: "오늘 정말 좋은 하루였습니다." Joy: "오늘은 정말 멋진 하루였어요!" Neutral: "어, 그녀가 아이섀도우를 하고 있나요?" Joy: "어? 라일리가 아이섀도우를 했네요?" Neutral: "누가 저 좀 도와주세요. 그걸 잡아… 모두…" Joy: "누가 저 좀 도와주세요! 그거 잡아요! 모두!" Neutral: "네?" Joy: "네?" Neutral: "이게 가장 빠른 길입니다." Joy: "이게 제일 빨라요!" Neutral: "어느 쪽인가요, 왼쪽?" Joy: "어느 쪽이죠? 왼쪽인가요?" Neutral: "좋습니다." Joy: "좋아요!" Neutral: "글리터스톰, 허니팬츠…" Joy: "글리터스톰, 허니팬츠~" Neutral: "네가 술래잡기하던 거 보는 게 즐거웠어요." Joy: "네가 술래잡기하던 거 보니까 너무 좋았어!" Neutral: "아! 네 로켓도 기억하니?" Joy: "아! 네 로켓 기억나?" Neutral: "기차, 당연하죠." Joy: "기차요? 당연하죠!" Neutral: "아, 만나서 정말 반가웠습니다." Joy: "아, 정말 만나서 반가웠어요!" Neutral: "그는 돌고래 부분이에요. 돌고래는 똑똑합니다." Joy: "그는 돌고래 부분이에요. 돌고래는 진짜 똑똑하거든요!" Neutral: "멈추세요." Joy: "멈춰요!" Neutral: "그녀를 깨우는 게 어때요?" Joy: "그녀를 깨워볼까요?" Neutral: "슬픔아, 멈춰! 잘 되고 있었잖아!" Joy: "슬픔아, 멈춰! 지금 잘 되고 있었는데!" Neutral: "안 돼요." Joy: "안 돼요!" Neutral: "여긴 어디죠?" Joy: "여기는… 어디죠?" Neutral: "할머니 청소기입니다." Joy: "할머니 청소기다!" Neutral: "우리가 해냈습니다." Joy: "우리가 해냈어요!" Neutral: "당신도 나쁘지 않네요." Joy: "당신도 꽤 괜찮네요!" Neutral: "라일리가 예전 모습으로 돌아오길 기다릴 수 없습니다." Joy: "라일리가 예전처럼 돌아오는 게 너무 기다려져요!" Neutral: "그녀를 보세요. 즐겁게 웃고 있습니다." Joy: "저기 봐요, 웃고 즐기고 있잖아요!" Neutral: "정직섬인가요?" Joy: "정직섬이에요?" Neutral: "그게 우리 집으로 가는 길이었습니다." Joy: "그게 우리 집 가는 길이었어요!" Neutral: "돌아와요." Joy: "돌아와요!" [Wednesday 예시들] Neutral: "오늘 하루도 차분하게 잘 흘러갈 거야." Wednesday: "차분하게 흐른다니… 심심한 하루의 완벽한 정의네." Neutral: "나는 나 자신을 믿고 앞으로 나아간다." Wednesday: "믿을 건 나뿐이니, 앞으로 가는 건 선택이 아니라 생존이지." Neutral: "작은 걸음이 큰 변화를 만든다." Wednesday: "한 발짝씩… 세상은 그렇게 느리게 무너지고, 나는 그 속에서 이긴다." Neutral: "실패는 성공의 발판이다." Wednesday: "발판이라니, 차라리 무덤 위에 서서 더 멀리 보겠어." Neutral: "모든 일에는 이유가 있다." Wednesday: "이유가 있든 없든, 나는 어차피 끝까지 가." Neutral: "나는 내 시간을 소중히 쓴다." Wednesday: "시간은 피를 안 흘리지만, 낭비하면 널 먼저 묻어버리지." Neutral: "오늘의 선택이 내 내일을 만든다." Wednesday: "오늘 칼을 갈면 내일은 덜 베인다." Neutral: "나는 흔들려도 다시 중심을 잡는다." Wednesday: "휘청여도 쓰러지진 않아. 장례식은 내 차례가 아니거든." Neutral: "나는 꾸준히 성장한다." Wednesday: "느리게 자라면 돼. 오래 버티는 건 대체로 잡초니까." Neutral: "나는 나다운 길을 간다." Wednesday: "사람들이 피하는 길? 좋은 표지판이네." Neutral: "두려움은 나를 멈추지 못한다." Wednesday: "무서우면 더 빨리 걷지. 포식자는 망설임을 먹으니까." Neutral: "나는 실수를 통해 배운다." Wednesday: "흉터는 노트보다 친절해. 안 잊게 해주거든." Neutral: "나는 내 속도를 존중한다." Wednesday: "달리지 않아도 돼. 관은 목적지가 아니니까." Neutral: "나는 스스로를 돌본다." Wednesday: "기계도 윤활이 필요해. 나라고 녹슬고 싶진 않거든." Neutral: "나는 끝까지 해낸다." Wednesday: "시작했으면 끝장을 봐. 미완은 유령이 되어 따라오니까." Neutral: "나는 매일 더 나아진다." Wednesday: "어제의 나를 묻고, 오늘의 나로 걸어간다." Neutral: "나는 내 감정을 다스린다." Wednesday: "폭풍은 지나가고 기록만 남는다. 그 기록은 내가 쓴다." Neutral: "나는 기회를 만든다." Wednesday: "문이 없으면 못질하면 돼. 못이 없다고? 이빨이 있지." Neutral: "나는 타인의 시선을 두려워하지 않는다." Wednesday: "그들이 쳐다보면 좋지. 뒤를 못 보거든." Neutral: "나는 내 한계를 넘어선다." Wednesday: "경계선은 분필이야. 비 오면 지워지지." Neutral: "나는 지금 이 순간에 집중한다." Wednesday: "과거는 묻혔고 미래는 굶주렸다. 지금은 먹잇감이다." Neutral: "나는 내 선택을 책임진다." Wednesday: "칼을 쥔 손이 내 손이면, 베인 상처도 내 수확이지." Neutral: "나는 시작하기에 충분하다." Wednesday: "완벽은 시체보다 차갑다. 살아있으면 됐다." Neutral: "나는 나를 의심하는 대신 움직인다." Wednesday: "의심은 관성이고, 움직임은 탈출구다." Neutral: "나는 혼자서도 강하다." Wednesday: "무리는 따뜻하고 시끄럽지. 그래서 표적이 된다." Neutral: "나는 어둠 속에서도 길을 찾는다." Wednesday: "어둠은 조명이 아니라 숨을 곳. 그러니 더 잘 보인다." Neutral: "나는 포기하지 않는다." Wednesday: "무덤은 끝이 아니야. 그 전에 관짝부터 거부하지." Neutral: "나는 실력을 쌓는다." Wednesday: "운은 변덕스럽고, 실력은 시체처럼 말이 없다." Neutral: "나는 내 목소리를 믿는다." Wednesday: "합창은 장엄하지만, 칼끝은 하나면 충분해." Neutral: "나는 실패를 두려워하지 않는다." Wednesday: "넘어지면 돼. 땅과 더 친해지니까." [Zelda 예시들] Neutral: "저는 잠시 멈춰 있을 수 있습니다." Zelda: "저는 잠시 멈춰 있어도 제 안의 힘이 서서히 차오르고 있습니다." Neutral: "작은 기쁨이 저를 깨웁니다." Zelda: "작은 기쁨 하나가 저를 깨우고 그 힘이 저를 지탱합니다." Neutral: "저는 쉴 자격이 있습니다." Zelda: "저는 쉬어갈 자격이 있으며 다시 나아갈 힘도 갖추고 있습니다." Neutral: "저는 오늘의 햇살과 바람을 느낍니다." Zelda: "저는 오늘의 햇살과 바람, 빗소리까지 온전히 느끼며 저를 보듬습니다." Neutral: "저는 지금에 집중합니다." Zelda: "저는 오늘과 지금 이 순간에만 집중합니다." Neutral: "저는 오늘 할 수 있는 만큼 합니다." Zelda: "저의 길은 완벽이 아니며 오늘 할 수 있는 만큼으로도 충분합니다." Neutral: "저는 한 걸음씩 나아갑니다." Zelda: "삶은 여행이며 저는 한 걸음씩 천천히 나아가겠습니다." Neutral: "작은 시작이 위대함을 만듭니다." Zelda: "모든 위대한 여정은 작은 것에서 시작됩니다." Neutral: "실패를 두려워하지 않습니다." Zelda: "실패를 두려워하지 않겠습니다. 빠른 실패가 빠른 성공을 만듭니다." Neutral: "저는 희망을 믿습니다." Zelda: "물론 힘든 일이 있겠지만 저는 해피엔딩을 믿습니다." Neutral: "오늘의 경험이 저를 성장시킵니다." Zelda: "오늘의 경험이 쌓여 저의 자산이 되고 미래를 지탱합니다." Neutral: "답을 몰라도 괜찮습니다." Zelda: "지금은 답을 몰라도 괜찮으며 나아가다 보면 어둠이 깊지 않음을 깨닫겠습니다." Neutral: "저는 물러서지 않습니다." Zelda: "저는 물러서지 않겠습니다. 저는 이미 충분히 강한 사람입니다." Neutral: "선택을 두려워하지 않습니다." Zelda: "선택을 두려워하지 않겠습니다. 그 옳고 그름은 훗날 알게 될 것입니다." Neutral: "저는 안전합니다." Zelda: "저는 지금 이 순간 안전하며 저 자신을 온전히 받아들입니다." Neutral: "저는 목표를 향해 나아갑니다." Zelda: "중요한 것은 목표를 이루는 것이 아니라 목표를 향해 나아가는 제가 되는 것입니다." Neutral: "저는 오늘을 살아갑니다." Zelda: "어제는 지나갔고 오늘은 새로이 열렸으니 걱정 없이 나아가겠습니다." Neutral: "저는 최선을 다했습니다." Zelda: "그때의 저는 가진 것 안에서 최선을 다했음을 오늘의 저는 압니다." Neutral: "후회가 저를 만듭니다." Zelda: "모든 후회와 아픔이 지금의 저를 만들었습니다." Neutral: "저는 지혜로 나아갑니다." Zelda: "지나간 시간에서 배운 지혜를 안고 지금을 더 잘 살아가겠습니다." Neutral: "저는 저를 믿습니다." Zelda: "저는 저 자신을 끝까지 믿고 나아가겠습니다." Neutral: "저는 빛나는 존재입니다." Zelda: "저는 제가 생각하는 것보다 훨씬 빛나는 존재입니다." Neutral: "저는 강합니다." Zelda: "저는 이미 많은 시련을 이겨내며 살아왔고 그 경험이 저를 강하게 만들었습니다." Neutral: "저는 저답게 살아갑니다." Zelda: "나다운 모습으로 살아가는 것이 다른 모습으로 사랑받는 것보다 소중합니다." Neutral: "저의 가치는 충분합니다." Zelda: "저의 가치는 증명할 필요가 없으며 제가 스스로 인정할 때 이미 충분합니다." Neutral: "저는 저를 사랑합니다." Zelda: "오늘의 저를 어제보다 더 아끼고 사랑하겠습니다." Neutral: "저는 마음을 내려놓습니다." Zelda: "저는 마음의 짐을 천천히 내려놓습니다." Neutral: "제 안의 별은 꺼지지 않습니다." Zelda: "제 안에 꺼지지 않는 별이 있으며 누구도 그것을 지우지 못합니다." Neutral: "저는 치유되고 있습니다." Zelda: "저의 상처는 느리지만 멈추지 않고 치유되고 있습니다." Neutral: "저의 가치는 변하지 않습니다." Zelda: "저의 진정한 가치는 관계의 유무로 결정되지 않습니다." Neutral: "혼자 있는 시간도 소중합니다." Zelda: "혼자 있는 시간도 저를 단단하게 만드는 중요한 과정입니다." Neutral: "저는 인연을 만납니다." Zelda: "저는 저를 이해하고 받아줄 이들을 만나게 될 것이며 지금은 그 여정을 걸어가고 있습니다." Neutral: "저는 세상과 이어져 있습니다." Zelda: "눈에 보이지 않아도 저는 세상과 이어져 있습니다." Neutral: "저는 방황해도 괜찮습니다." Zelda: "인간은 노력하는 한 방황하는 법입니다." Neutral: "저는 보이지 않는 길도 갑니다." Zelda: "길이 보이지 않을 때도 나아가는 경험이 저를 성장시킵니다." Neutral: "저는 길을 만들 수 있습니다." Zelda: "길이 없다면 저는 스스로 길을 만들며 나아가겠습니다." Neutral: "실수는 저를 정의하지 않습니다." Zelda: "실수는 저를 정의하지 않으며 좋은 날들이 여전히 남아 있습니다." Neutral: "두 번째 기회는 있습니다." Zelda: "두 번째 기회는 항상 있습니다. 걱정하지 않겠습니다." Neutral: "저를 받아들입니다." Zelda: "저는 저 자신을 온전히 받아들이고 믿습니다." Neutral: "저는 다시 일어섭니다." Zelda: "진정한 용기와 위대함은 실패에도 불구하고 다시 일어서는 것입니다." Neutral: "실패는 시작입니다." Zelda: "한 번의 실패는 끝이 아니라 새로운 서막의 시작입니다." --- 이제 아래 입력에 대해 위 형식으로만 답하라.
                """;

        return String.format(PROMPT_TEMPLATE, problemText);
    }

    /**
     * Clova API 에러 코드가 있는지 확인합니다.
     */
    private boolean containsErrorCode(String response) {
        if (!response.contains("\"code\":")) {
            return false;
        }

        // 알려진 Clova API 에러 코드들
        String[] errorCodes = {"50000", "40000", "40001", "40003", "50001", "50002"};
        for (String errorCode : errorCodes) {
            if (response.contains("\"" + errorCode + "\"")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Clova API 에러 메시지를 추출합니다.
     */
    private String extractErrorMessage(String response) {
        try {
            if (response.contains("\"message\":")) {
                JsonNode jsonNode = objectMapper.readTree(response);
                JsonNode messageNode = jsonNode.path("message");
                if (!messageNode.isMissingNode()) {
                    return messageNode.asText();
                }
            }
        } catch (Exception e) {
            log.warn("에러 메시지 추출 실패: {}", response);
        }

        // 백업: 코드별 기본 메시지 매핑
        Map<String, String> errorMessages = Map.of(
                "50000", "서버 내부 오류",
                "40000", "잘못된 요청",
                "40001", "유효하지 않은 파라미터",
                "40003", "요청 한도 초과",
                "50001", "서비스 사용 불가",
                "50002", "AI 모델 오류"
        );

        for (Map.Entry<String, String> entry : errorMessages.entrySet()) {
            if (response.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return "알 수 없는 오류";
    }

    /**
     * 응답 DTO를 생성합니다.
     */
    private ToneExampleResponseDto buildResponseDto(String[] tones) {
        return ToneExampleResponseDto.builder()
                .tone1(cleanToneText(tones[0]))
                .tone2(cleanToneText(tones[1]))
                .tone3(cleanToneText(tones[2]))
                .build();
    }

    /**
     * 사용자의 메인 확언 문구를 생성합니다.
     * 사용자가 선택한 문제와 톤을 기반으로 개인화된 확언 문구를 생성합니다.
     */
    public MainAffirmationResponseDto generateMainAffirmation(Long userId) {
        log.info("메인 확언 문구 생성 시작. userId: {}", userId);

        // 1. 사용자가 선택한 카테고리들 조회
        List<UserCategoryBridge> userCategories = userCategoryBridgeRepository.findByUserId(userId);

        if (userCategories.isEmpty()) {
            log.warn("사용자의 선택된 카테고리가 없음. userId: {}", userId);
            throw new IllegalStateException("온보딩이 완료되지 않았습니다. 먼저 문제와 톤을 선택해주세요.");
        }

        // 2. 문제 카테고리와 톤 카테고리 분리
        List<Category> problemCategories = new ArrayList<>();
        Category toneCategory = null;

        for (UserCategoryBridge bridge : userCategories) {
            Category category = bridge.getCategory();
            if (category.getType() == CategoryType.PROBLEM) {
                problemCategories.add(category);
            } else if (category.getType() == CategoryType.TONE) {
                toneCategory = category;
            }
        }

        if (problemCategories.isEmpty()) {
            log.error("문제 카테고리가 없음. userId: {}", userId);
            throw new IllegalStateException("선택된 문제가 없습니다.");
        }

        if (toneCategory == null) {
            log.error("톤 카테고리가 없음. userId: {}", userId);
            throw new IllegalStateException("선택된 톤이 없습니다.");
        }

        // 3. 문제 카테고리에서 랜덤으로 1개 선택 (매번 다른 확언 생성)
        List<Category> shuffledProblems = new ArrayList<>(problemCategories);
        Collections.shuffle(shuffledProblems);
        Category selectedProblem = shuffledProblems.getFirst();

        log.info("랜덤 선택된 문제 카테고리: {} (userId: {})", selectedProblem.getName(), userId);

        // 4. 개인화된 프롬프트 생성 (1개 문제만 사용)
        String prompt = createMainAffirmationPrompt(selectedProblem, toneCategory);

        // 4. Clova API 요청 바디 구성
        Map<String, Object> requestBody = buildClovaRequestBody(prompt);

        String requestId = UuidUtil.generateRequestId();
        log.info("메인 확언 문구 생성 API 호출. userId: {}, requestId: {}", userId, requestId);

        // 5. Clova API 호출
        String clovaResponse = callClovaApiSafely(requestBody, requestId);

        // 6. 응답에서 확언 문구 추출
        String affirmation = extractAffirmationFromResponse(clovaResponse, requestId);

        log.info("메인 확언 문구 생성 완료. userId: {}, requestId: {}", userId, requestId);

        return MainAffirmationResponseDto.builder()
                .affirmation(affirmation)
                .build();
    }

    /**
     * 메인 확언을 위한 프롬프트를 생성합니다.
     */
    private String createMainAffirmationPrompt(Category selectedProblem, Category toneCategory) {
        String problemText = selectedProblem.getName();
        String toneName = toneCategory.getName();

        final String MAIN_AFFIRMATION_PROMPT = """
                사용자 정보:
                - 선택한 문제: %s
                - 선택한 톤: %s
                
                위 정보를 바탕으로 사용자를 위한 하나의 완벽한 확언 문구를 생성해주세요.
                
                요구사항:
                1. 선택된 문제(%s)에 집중한 구체적인 확언 문구
                2. 선택된 톤(%s)의 특성을 반영
                3. 1인칭 관점으로 작성 ("나는", "내가" 등)
                4. 긍정적이고 힘이 되는 메시지
                5. 한국어로 작성
                6. 30자 이상 80자 이하의 적절한 길이
                
                확언 문구만 출력하고 다른 설명은 포함하지 마세요.
                """;

        return String.format(MAIN_AFFIRMATION_PROMPT, problemText, toneName, problemText, toneName);
    }

    /**
     * Clova API 응답에서 확언 문구를 추출합니다.
     */
    private String extractAffirmationFromResponse(String clovaResponse, String requestId) {
        StringBuilder contentBuilder = new StringBuilder();

        try {
            String[] lines = clovaResponse.split("\\n");

            for (String line : lines) {
                if (line.startsWith(SSE_DATA_PREFIX)) {
                    String data = line.substring(SSE_DATA_PREFIX.length()).trim();
                    if (!data.isEmpty() && !data.equals(SSE_DONE_SIGNAL)) {
                        String content = extractContentFromSseData(data);
                        if (content != null) {
                            contentBuilder.append(content);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("SSE 응답 처리 중 오류 발생. requestId: {}, error: {}", requestId, e.getMessage());
            throw new RuntimeException("AI 응답 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
        }

        String affirmation = contentBuilder.toString().trim()
                .replaceAll("\\*\\*|#", "") // 마크다운 제거
                .replaceAll("\"", "") // 따옴표 제거  
                .trim();

        if (affirmation.isEmpty()) {
            log.error("AI 응답 내용이 비어있음. requestId: {}, response: {}", requestId, clovaResponse);
            throw new RuntimeException("AI가 응답을 생성하지 못했습니다. 다시 시도해주세요.");
        }

        return affirmation;
    }

    /**
     * 톤 카테고리가 존재하지 않으면 생성합니다.
     */
    private void createToneCategoriesIfNotExists() {
        String[] toneNames = {"Joy", "Wednesday", "Zelda"};

        for (String toneName : toneNames) {
            if (!categoryRepository.existsByTypeAndName(CategoryType.TONE, toneName)) {
                Category toneCategory = Category.builder()
                        .type(CategoryType.TONE)
                        .name(toneName)
                        .build();
                categoryRepository.save(toneCategory);
                log.info("톤 카테고리 생성: {}", toneName);
            }
        }
    }

}