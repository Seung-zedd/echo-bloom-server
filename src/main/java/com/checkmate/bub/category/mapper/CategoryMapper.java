package com.checkmate.bub.category.mapper;

import com.checkmate.bub.category.domain.Category;
import com.checkmate.bub.category.dto.CategoryRequestDto;
import com.checkmate.bub.category.dto.CategoryResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

//* 빌드 시 매핑 안된 필드를 로그로 찾기 위해 WARN으로 설정하는 것이 좋음(BaseEntity를 상속한 클래스들은 의도적으로 IGNORE로 설정하였음.)
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    Category to(CategoryRequestDto categoryRequestDto);
    CategoryResponseDto from(Category category);
    List<CategoryResponseDto> fromList(List<Category> categoryList);
}
