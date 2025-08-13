package com.checkmate.bub.util;

import java.util.UUID;

public class UuidUtil {
    public static String generateRequestId() {
        return UUID.randomUUID().toString();
    }
}
