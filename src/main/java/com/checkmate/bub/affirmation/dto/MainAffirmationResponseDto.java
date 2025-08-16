package com.checkmate.bub.affirmation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MainAffirmationResponseDto {
    private String affirmation1;
    private String affirmation2;
    private String affirmation3;
}