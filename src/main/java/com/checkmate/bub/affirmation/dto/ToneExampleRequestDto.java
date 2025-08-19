package com.checkmate.bub.affirmation.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ToneExampleRequestDto {
    
    @NotEmpty(message = "문제 ID 목록은 비어 있을 수 없습니다.")
    @Size(max = 3, message = "최대 3개의 문제만 선택할 수 있습니다.")
    private List<Long> problems;
    
    private String tone;
}