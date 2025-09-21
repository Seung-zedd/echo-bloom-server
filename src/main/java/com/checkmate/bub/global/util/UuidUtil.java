package com.checkmate.bub.global.util;

import java.util.UUID;

// BoilerPlate Class
public class UuidUtil {
    public static String generateRequestId() {
        return UUID.randomUUID().toString();
    }
}
