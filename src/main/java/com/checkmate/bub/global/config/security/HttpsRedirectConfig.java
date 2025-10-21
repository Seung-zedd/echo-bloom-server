package com.checkmate.bub.global.config.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;

/**
 * HTTPS 리다이렉트 설정 (선택적)
 *
 * 이 설정은 HTTP(8080) 요청을 HTTPS(443)로 자동 리다이렉트합니다.
 * AWS Lightsail의 Load Balancer를 사용하는 경우에는 이 설정이 필요하지 않을 수 있습니다.
 *
 * 활성화 방법:
 * application-prod.yml에 다음 설정 추가:
 * server:
 *   http:
 *     redirect-to-https: true
 */
@Configuration
@ConditionalOnProperty(name = "server.http.redirect-to-https", havingValue = "true")
public class HttpsRedirectConfig {

    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addAdditionalTomcatConnectors(createHttpConnector());
        return tomcat;
    }

    private Connector createHttpConnector() {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(8080);
        connector.setSecure(false);
        connector.setRedirectPort(443);  // HTTP(8080) → HTTPS(443) 리다이렉트
        return connector;
    }
}
