package com.checkmate.bub.ai.clova;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

// Clova Speech Recognition (STT) 전용 클라이언트
//todo: Clova Speech Recognition 서비스를 신청 및 사용할 때 정확한 url을 입력할 것
@FeignClient(name = "clova-speech", url = "https://naveropenapi.apigw.ntruss.com/recog/v1/stt")
public interface ClovaSpeechClient {

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Object> recognizeSpeech(
            @RequestPart("audioFile") MultipartFile audioFile,         // 오디오 데이터 (WAV/MP3 등)
            @RequestHeader("X-NCP-APIGW-API-KEY-ID") String apiKeyId,  // Speech 전용 API 키 ID
            @RequestHeader("X-NCP-APIGW-API-KEY") String apiKey,       // Speech 전용 API 키
            @RequestHeader("X-CLOVASPEECH-LANGUAGE") String language   // "ko-KR" 또는 "en-US"
    );
}
