package com.checkmate.bub.domain.ai.clova;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

// Clova Speech Recognition (STT) 전용 클라이언트
@FeignClient(name = "clova-speech", url = "https://naveropenapi.apigw.ntruss.com/recog/v1/stt")
public interface ClovaSpeechClient {

    @PostMapping(consumes = "application/octet-stream", produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Object> recognizeSpeech(
            @RequestHeader("X-NCP-APIGW-API-KEY-ID") String apiKeyId,  // 앱 등록 시 발급받은 Client ID
            @RequestHeader("X-NCP-APIGW-API-KEY") String apiKey,       // 앱 등록 시 발급받은 Client Secret
            @RequestParam("lang") String language,                      // "Kor" 또는 "Eng"
            @RequestBody byte[] audioData                               // 오디오 파일 데이터 (application/octet-stream)
    );
}
