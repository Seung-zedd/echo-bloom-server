package com.checkmate.bub.util;

import java.util.UUID;

// BoilerPlate Class
public class UuidUtil {
    public static String generateRequestId() {
        return UUID.randomUUID().toString();
    }
}
