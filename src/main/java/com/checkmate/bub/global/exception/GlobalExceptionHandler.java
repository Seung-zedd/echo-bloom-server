package com.checkmate.bub.global.exception;

import com.checkmate.bub.util.EnvironmentUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final EnvironmentUtil envUtil;

    // 엔티티를 찾을 수 없을 때 (사용자가 존재하지 않는 경우 등)
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException e) {
        return createErrorResponse(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다.", e.getMessage());
    }

    // 필수 파라미터가 누락되었을 때 (code 파라미터 누락 등)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParameter(MissingServletRequestParameterException e) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "필수 파라미터가 누락되었습니다.", e.getMessage());
    }

    // @Valid 어노테이션이 붙은 @RequestBody DTO의 유효성 검증이 실패했을 때만 발생
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException e) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다.", e.getMessage());
    }

    // 외부 API 호출 실패 시
    @ExceptionHandler({HttpClientErrorException.class, WebClientResponseException.class,
            AuthenticationServiceException.class})
    public ResponseEntity<Map<String, Object>> handleExternalApiError(Exception e) {
        log.error("External API call failed", e);
        return createErrorResponse(HttpStatus.BAD_REQUEST, "외부 API 호출에 실패했습니다.", "서비스 연결에 문제가 발생했습니다.");
    }

    // IllegalArgumentException 처리
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.", e.getMessage());
    }

    // 클라이언트 연결 중단 예외 처리 (ClientAbortException)
    @ExceptionHandler(ClientAbortException.class)
    public ResponseEntity<Map<String, Object>> handleClientAbort(ClientAbortException e) {
        log.warn("Client disconnected during request processing: {}", e.getMessage());
        // 클라이언트가 이미 연결을 끊었으므로 응답을 보낼 수 없지만, 로깅 목적으로 ResponseEntity 반환
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).build();
    }

    // 기타 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        // ClientAbortException의 경우 이미 별도 처리했으므로 로그 레벨 조정
        if (e instanceof ClientAbortException || e.getCause() instanceof ClientAbortException) {
            log.warn("Client abort exception in generic handler: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).build();
        }
        
        log.error("Unexpected error occurred", e);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.", e.getMessage());
    }

    // 공통 에러 응답 생성 메서드
    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String message, String details) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        //! 운영 환경에서는 상세 정보 노출 방지
        if (envUtil.isLocalEnvironment() || envUtil.isDevEnvironment()) {
            errorResponse.put("details", details);
        }

        // Content-Type을 명시적으로 application/json으로 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        return ResponseEntity.status(status).headers(headers).body(errorResponse);
    }

    /**
     * 데이터 무결성 제약 조건 위반 시 (예: 사용 중인 카테고리 삭제 시도)
     * @param e DataIntegrityViolationException
     * @return 409 Conflict 응답
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("Data integrity violation", e);
        return createErrorResponse(HttpStatus.CONFLICT, "데이터 무결성 제약으로 인해 요청을 수행할 수 없습니다.", "무결성 제약 조건을 위반했습니다.");
    }

    // 그 외 모든 파라미터 입력 검증 실패
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(jakarta.validation.ConstraintViolationException e) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "입력값이 유효하지 않습니다.", "검증 제약을 위반했습니다.");
    }

}
