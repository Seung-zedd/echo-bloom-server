package com.checkmate.bub.domain.ai.clova;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;



@FeignClient(name = "clova", url = "https://clovastudio.stream.ntruss.com/v3/chat-completions/HCX-005")
public interface ClovaClient {

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "text/event-stream")
    String callApi(
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String authorization,  // "Bearer " + apiKey 형태로 전달
            @RequestHeader("X-NCP-CLOVASTUDIO-REQUEST-ID") String requestId,
            @RequestHeader("Content-Type") String contentType,  // "application/json"
            @RequestHeader("Accept") String accept  // "text/event-stream"
    );
}
