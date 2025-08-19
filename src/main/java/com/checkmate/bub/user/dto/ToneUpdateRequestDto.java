package com.checkmate.bub.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ToneUpdateRequestDto {

    @NotBlank(message = "톤 이름은 필수입니다.")
    private String toneName;
}