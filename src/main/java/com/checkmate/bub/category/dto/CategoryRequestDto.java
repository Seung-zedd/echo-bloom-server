package com.checkmate.bub.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequestDto {

    @NotBlank(message = "카테고리 타입은 필수 입력값입니다.")
    @Size(max = 255, message = "카테고리 타입은 255자 이하여야 합니다.")
    private String type;

    @NotBlank(message = "카테고리 이름은 필수 입력값입니다.")
    @Size(max = 255, message = "카테고리 이름은 255자 이하여야 합니다.")
    private String name;
}
