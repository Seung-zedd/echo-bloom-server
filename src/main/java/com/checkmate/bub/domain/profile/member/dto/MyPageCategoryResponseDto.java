package com.checkmate.bub.domain.profile.member.dto;

import java.util.List;

public record MyPageCategoryResponseDto(
        List<CategorySelectionDto> problems,
        CategorySelectionDto tone
) {
}
