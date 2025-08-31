package com.checkmate.bub.util;

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
        return Arrays.asList(environment.getActiveProfiles()).contains("local") ||
               Arrays.asList(environment.getActiveProfiles()).contains("dev");
    }

}
