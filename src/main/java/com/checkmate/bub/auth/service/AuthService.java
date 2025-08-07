package com.checkmate.bub.auth.service;

import com.checkmate.bub.auth.dto.AuthResponseDto;
import com.checkmate.bub.auth.dto.KakaoTokenResponseDto;
import com.checkmate.bub.auth.dto.KakaoUserInfoResponseDto;
import com.checkmate.bub.global.jwt.JwtTokenProvider;
import com.checkmate.bub.user.domain.User;
import com.checkmate.bub.user.mapper.UserMapper;
import com.checkmate.bub.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final WebClient webClient;
    private final UserMapper userMapper;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;
    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String tokenUri;
    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String userInfoUri;

    @Transactional
    public AuthResponseDto loginWithKakao(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Authorization code is required");
        }

        try {
            // 1. 인가 코드로 카카오에 액세스 토큰을 요청합니다.
            KakaoTokenResponseDto tokenResponse = getKakaoToken(code);

            // 2. 액세스 토큰으로 카카오에 사용자 정보를 요청합니다.
            KakaoUserInfoResponseDto userInfo = getKakaoUserInfo(tokenResponse.getAccessToken());

            // 3. 받은 사용자 정보로 우리 서비스의 회원을 찾거나, 없으면 새로 가입시킵니다.
            User user = userRepository.findByKakaoId(userInfo.getId())
                    .orElseGet(() -> registerNewUser(userInfo));

            // 4. 우리 서비스의 자체 JWT를 생성하여 반환합니다.
            String accessToken = jwtTokenProvider.createAccessToken(user.getId());
            String refreshToken = jwtTokenProvider.createRefreshToken(user.getId()); // 필요 시 리프레시 토큰도 생성

            return AuthResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        } catch (WebClientResponseException e) {
            log.error("Kakao API call failed: {}", e.getMessage());
            throw new AuthenticationServiceException("카카오 API 호출에 실패했습니다.");
        }

    }

    // 카카오에 토큰 요청
    private KakaoTokenResponseDto getKakaoToken(String code) {
        return webClient.post()
                .uri(tokenUri)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .bodyValue("grant_type=authorization_code"
                        + "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                        + "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)
                        + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                        + "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8))
                .retrieve()
                .bodyToMono(KakaoTokenResponseDto.class)
                .timeout(Duration.ofSeconds(5))
                .onErrorMap(Exception.class, e -> {
                    log.error("Falied to get Kakao token", e);
                    return new AuthenticationServiceException("카카오 토큰 획득 실패");
                })
                .block();
    }

    // 카카오에 사용자 정보 요청
    private KakaoUserInfoResponseDto getKakaoUserInfo(String accessToken) {
        return webClient.get()
                .uri(userInfoUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoUserInfoResponseDto.class)
                .timeout(Duration.ofSeconds(5))
                .onErrorMap(Exception.class, e -> {
                    log.error("Falied to get Kakao token", e);
                    return new AuthenticationServiceException("카카오 토큰 획득 실패");
                })
                .block();
    }

    // 신규 회원 등록 (MapStruct를 사용하도록 수정)
    private User registerNewUser(KakaoUserInfoResponseDto userInfo) {
        User newUser = userMapper.kakaoDtoToUser(userInfo);
        return userRepository.save(newUser);
    }
}
