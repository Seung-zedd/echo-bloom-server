package com.checkmate.bub.category.service;

import com.checkmate.bub.category.domain.Category;
import com.checkmate.bub.category.dto.CategoryRequestDto;
import com.checkmate.bub.category.dto.CategoryResponseDto;
import com.checkmate.bub.category.mapper.CategoryMapper;
import com.checkmate.bub.category.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
//? 데이터를 변경할 필요가 없다고 판단하여, JPA는 스냅샷을 만들거나 변경을 감지하는 등의 불필요한 내부 동작을 생략합니다. 이 덕분에 조회 성능이 눈에 띄게 향상됩니다.
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    // 조회 메소드: 클래스 설정을 상속받아 readOnly = true로 동작
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
        Category category = categoryMapper.to(categoryRequestDto);
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.from(savedCategory);
    }

    @Transactional
    public CategoryResponseDto update(Long categoryId, CategoryRequestDto categoryRequestDto) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("ID " + categoryId + "에 해당하는 카테고리를 찾을 수 없습니다."));
        category.update(categoryRequestDto.getType(), categoryRequestDto.getName());

        return categoryMapper.from(category);
    }

    @Transactional
    public void delete(Long categoryId) {
        // ID로 엔티티가 존재하는지 먼저 확인 후 삭제하는 것이 더 안전합니다.
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("ID " + categoryId + "에 해당하는 카테고리를 찾을 수 없습니다."));
        categoryRepository.delete(category);
    }
}
