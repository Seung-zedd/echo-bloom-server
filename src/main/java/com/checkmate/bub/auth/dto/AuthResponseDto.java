package com.checkmate.bub.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponseDto {

    private String accessToken;
    private String refreshToken;
}
