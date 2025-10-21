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
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.*;  // 이 import 추가![1]
import org.springframework.beans.factory.config.*;  // ObjectPostProcessor import

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${spring.profiles.active:local}")  // 기본 local
    private String activeProfile;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정을 가장 먼저 적용 (preflight 처리 우선순위)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // REST API 이므로, CSRF 보안 기능 비활성화
                .csrf(AbstractHttpConfigurer::disable)

                // 세션을 사용하지 않으므로, 세션 관리 정책을 STATELESS로 설정
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 예외 처리 설정 (인증/인가 실패 시)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요합니다"))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendError(HttpServletResponse.SC_FORBIDDEN, "접근이 거부되었습니다"))
                )

                // HTTP 요청에 대한 접근 권한 설정
                .authorizeHttpRequests(authorize -> {
                    // OPTIONS 메서드 (CORS preflight)를 가장 먼저 허용 – 401 에러 방지
                    authorize.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

                    if ("local".equals(activeProfile)) {
                        authorize.requestMatchers("/v3/api-docs", "/swagger-ui/**").permitAll();
                    }

                    authorize
                            // 카카오 로그인 처리 API 경로는 인증 없이 모두 허용
                            .requestMatchers("/auth/kakao/callback").permitAll()
                            .requestMatchers("/auth/kakao/login-url").permitAll()
                            .requestMatchers("/api/check-auth").permitAll();

                    // 테스트용 인증 API 허용 (local 환경에서만 공개)
                    if ("local".equals(activeProfile)) {
                        authorize.requestMatchers("/test/auth/**").permitAll();
                    }

                    authorize
                            // 익명 사용자용 리소스 (메인 랜딩 페이지용만)
                            .requestMatchers("/", "/main.html", "/landing_page.html", "/app.js", "/*.css", "/img/**",
                                    "/music/**", "/css/**", "/error", "/favicon.ico").permitAll()

                            // 인증된 사용자용 리소스 (로그인 후 접근 가능)
                            .requestMatchers("/intro_start.html", "/home.html", "/app_*.js", "/views/**").authenticated()

                            // .well-known 경로 허용 추가 (에러 방지)
                            .requestMatchers("/.well-known/**").permitAll()

                            // 위에서 지정한 경로 외의 모든 요청은 반드시 인증(로그인) 필요
                            .anyRequest().authenticated();
                })

                // (중요) 우리가 직접 만든 JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 앞에 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // CORS 세부 설정 (허용 origin, method 등) - 환경별 분리
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 환경별 허용 origin 설정
        List<String> allowedOrigins = new ArrayList<>();
        List<String> allowedOriginPatterns = new ArrayList<>();

        if ("dev".equals(activeProfile) || "local".equals(activeProfile)) {
            // 개발 환경: localhost 와일드카드 패턴 허용
            allowedOriginPatterns.add("http://localhost:*");
            allowedOriginPatterns.add("http://127.0.0.1:*");
            // AWS Lightsail 배포용 HTTPS origin 추가 (dev 프로필)
            allowedOrigins.add("https://echobloom.co.kr");
            allowedOrigins.add("https://www.echobloom.co.kr");
        } else {
            // 프로덕션 환경: HTTPS만 허용 (HTTP 제거)
            allowedOrigins.add("https://echobloom.co.kr");
            allowedOrigins.add("https://www.echobloom.co.kr");
        }

        // 설정 적용
        if (!allowedOrigins.isEmpty()) {
            configuration.setAllowedOrigins(allowedOrigins);
        }
        if (!allowedOriginPatterns.isEmpty()) {
            configuration.setAllowedOriginPatterns(allowedOriginPatterns);
        }

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(List.of("*"));              // 모든 헤더 허용
        configuration.setAllowCredentials(true);                    // 쿠키/credentials 허용
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setMaxAge(3600L);                             // preflight 캐시 시간 (1시간)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);     // 모든 경로에 적용
        return source;
    }

    // .well-known 완전 무시 (옵션, Security 우회)
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/.well-known/**");
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // getClaim 메서드 사용 – Object로 가져와 안전 캐스팅
            Object scopeClaim = jwt.getClaim("scope");  // "scope" 클레임 가져오기 (Kakao 토큰 맞춤)
            List<String> scopes = new ArrayList<>();

            if (scopeClaim instanceof List<?> list) {
                scopes = list.stream()
                        .filter(item -> item instanceof String)
                        .map(item -> (String) item)
                        .collect(Collectors.toList());
            } else if (scopeClaim instanceof String str) {
                scopes = List.of(str.split(" "));  // 문자열 split (e.g., "scope1 scope2")
            }  // else: 빈 리스트 (클레임 없음 – 에러 방지)

            // scopes를 GrantedAuthority로 변환
            return scopes.stream()
                    .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))  // prefix 커스텀 (필요 시 "ROLE_")
                    .collect(Collectors.toList());
        });

        return converter;
    }

}
