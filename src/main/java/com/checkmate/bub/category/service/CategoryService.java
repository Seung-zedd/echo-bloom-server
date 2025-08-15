package com.checkmate.bub.category.service;

import com.checkmate.bub.category.constant.CategoryType;
import com.checkmate.bub.category.domain.Category;
import com.checkmate.bub.category.dto.CategoryRequestDto;
import com.checkmate.bub.category.dto.CategoryResponseDto;
import com.checkmate.bub.category.mapper.CategoryMapper;
import com.checkmate.bub.category.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
//? 데이터를 변경할 필요가 없다고 판단하여, JPA는 스냅샷을 만들거나 변경을 감지하는 등의 불필요한 내부 동작을 생략합니다. 이 덕분에 조회 성능이 눈에 띄게 향상됩니다.
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryResponseDto> findAll() {
        List<Category> categoryList = categoryRepository.findAll();
        return categoryMapper.fromList(categoryList);
    }

    public CategoryResponseDto findOne(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("해당 ID의 카테고리를 찾을 수 없습니다: " + id));
        return categoryMapper.from(category);
    }

    @Transactional
    public CategoryResponseDto create(CategoryRequestDto categoryRequestDto) {
        // String to enum 캐스팅
        CategoryType type;
        try {
            type = CategoryType.valueOf(categoryRequestDto.getType().toUpperCase());  // 대문자 변환으로 유연성 ↑
        } catch (IllegalArgumentException e) {
            log.error("Invalid type: {}", categoryRequestDto.getType());
            throw new IllegalArgumentException("유효하지 않은 타입입니다: " + categoryRequestDto.getType());
        }

        if (categoryRepository.existsByTypeAndName(type, categoryRequestDto.getName())) {
            log.error("type: {}, name: {}", type, categoryRequestDto.getName());
            throw new IllegalStateException("이미 존재하는 카테고리입니다.");
        }

        Category category = categoryMapper.to(categoryRequestDto);
        category.update(type, categoryRequestDto.getName());  // enum으로 업데이트 (엔티티 메서드 가정)
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.from(savedCategory);
    }

    @Transactional
    public CategoryResponseDto update(Long categoryId, CategoryRequestDto categoryRequestDto) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("ID " + categoryId + "에 해당하는 카테고리를 찾을 수 없습니다."));

        // String to enum 캐스팅
        CategoryType type;
        try {
            type = CategoryType.valueOf(categoryRequestDto.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid type: {}", categoryRequestDto.getType());
            throw new IllegalArgumentException("유효하지 않은 타입입니다: " + categoryRequestDto.getType());
        }

        if (categoryRepository.existsByTypeAndNameAndIdNot(type, categoryRequestDto.getName(), categoryId)) {
            log.error("type: {}, name: {}", type, categoryRequestDto.getName());
            throw new IllegalStateException("이미 존재하는 카테고리입니다.");
        }

        category.update(type, categoryRequestDto.getName());

        return categoryMapper.from(category);
    }

    @Transactional
    public void delete(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("ID " + categoryId + "에 해당하는 카테고리를 찾을 수 없습니다."));
        categoryRepository.delete(category);
    }
}
