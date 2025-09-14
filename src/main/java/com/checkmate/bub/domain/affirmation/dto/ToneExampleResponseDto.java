package com.checkmate.bub.domain.affirmation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ToneExampleResponseDto {
    private String tone1;
    private String tone2;
    private String tone3;
}
