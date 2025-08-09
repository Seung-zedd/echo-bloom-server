package com.checkmate.bub.global.config.security;

import com.checkmate.bub.global.jwt.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${springdoc.api-docs.enabled:true}")
    private boolean openApiEnabled;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private static final String[] SWAGGER_WHITELIST = {
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. REST API 이므로, CSRF 보안 기능 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                // CORS 설정 (프론트엔드 연동 시 필요)
                .cors(AbstractHttpConfigurer::disable) // 또는 적절한 CORS 설정

                // 2. 세션을 사용하지 않으므로, 세션 관리 정책을 STATELESS로 설정
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. 예외 처리 설정 (인증/인가 실패 시)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요합니다"))
                        .accessDeniedHandler((request, response, accessDeniedException) -> response.sendError(HttpServletResponse.SC_FORBIDDEN, "접근이 거부되었습니다"))
                )


                // 4. HTTP 요청에 대한 접근 권한 설정
                .authorizeHttpRequests(authorize -> {
                    // springdoc.api-docs.enabled가 true일 때만 Swagger 경로를 허용합니다.
                    if (openApiEnabled) {
                        authorize.requestMatchers(SWAGGER_WHITELIST).permitAll();
                    }

                    authorize
                            // 카카오 로그인 처리 API 경로는 인증 없이 모두 허용
                            .requestMatchers("/auth/kakao/**").permitAll()
                            // 비회원용 확언 체험 API 경로는 인증 없이 모두 허용
                            .requestMatchers("/api/affirmations/guest").permitAll()
                            // 카테고리 생성 API는 인증된 사용자만 접근 가능
                            .requestMatchers(HttpMethod.POST, "/api/v1/categories/**").authenticated()
                            // 위에서 지정한 경로 외의 모든 요청은 반드시 인증(로그인) 필요
                            .anyRequest().authenticated();
                })

                // 5. (중요) 우리가 직접 만든 JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 앞에 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
