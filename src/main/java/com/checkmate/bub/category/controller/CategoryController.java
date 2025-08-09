package com.checkmate.bub.category.controller;

import com.checkmate.bub.category.dto.CategoryRequestDto;
import com.checkmate.bub.category.dto.CategoryResponseDto;
import com.checkmate.bub.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getCategoryList() {
        List<CategoryResponseDto> categoryList = categoryService.findAll();
        return ResponseEntity.ok(categoryList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> getCategory(@PathVariable("id") Long categoryId) {
        CategoryResponseDto categoryResponseDto = categoryService.findOne(categoryId);
        return ResponseEntity.ok(categoryResponseDto);
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDto> createCategory(@Valid @RequestBody CategoryRequestDto categoryRequestDto) {
        CategoryResponseDto createdCategory = categoryService.create(categoryRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    // PUT은 멱등성(Idempotency)가 보장되기 때문에 주로 업데이트를 할 때 사용
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDto>
    updateCategory(@Valid @RequestBody CategoryRequestDto categoryRequestDto, @PathVariable("id") Long categoryId) {

        CategoryResponseDto updatedCategory = categoryService.update(categoryId, categoryRequestDto);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") Long categoryId) {
        // 2. 서비스에는 DTO가 아닌 ID만 전달합니다.
        categoryService.delete(categoryId);

        // 3. 삭제 성공 시, 내용이 없다는 의미의 204 No Content를 반환하는 것이 일반적입니다.
        return ResponseEntity.noContent().build();
    }
}
