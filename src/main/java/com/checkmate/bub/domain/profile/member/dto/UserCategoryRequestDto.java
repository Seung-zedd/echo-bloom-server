package com.checkmate.bub.domain.profile.member.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserCategoryRequestDto {

    @NotNull(message = "카테고리 ID 목록은 필수입니다.")
    @Size(min = 1, message = "최소 1개 이상의 카테고리를 선택해야 합니다.")
    private List<Long> categoryIds;

}