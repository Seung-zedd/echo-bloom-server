package com.checkmate.bub.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
// BoilerPlate Class
public class EnvironmentUtil {

    private final Environment environment;

    public boolean isLocalEnvironment() {
        return Arrays.asList(environment.getActiveProfiles()).contains("local");
    }

    public boolean isDevEnvironment() {
        return Arrays.asList(environment.getActiveProfiles()).contains("dev");
    }



    public boolean isHttpEnvironment() {
        // Only local environment uses HTTP
        // dev profile is now deployed on AWS Lightsail with HTTPS
        return Arrays.asList(environment.getActiveProfiles()).contains("local");
    }

}
