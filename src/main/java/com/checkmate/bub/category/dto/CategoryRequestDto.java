package com.checkmate.bub.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequestDto {

    @NotBlank(message = "카테고리 타입은 필수 입력값입니다.")
    private String type;

    @NotBlank(message = "카테고리 이름은 필수 입력값입니다.")
    private String name;
}
