package com.checkmate.bub.category.repository;

import com.checkmate.bub.category.constant.CategoryType;
import com.checkmate.bub.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


//* /Tone-Examples End Point to store three different tones with toneExampleResponseDto to store unselected tones (related to modifying tones in mypage > sentence invoked by 'updateUserCategories' method of UserController)
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // 중복 방지
    boolean existsByTypeAndName(CategoryType type, String name);
    boolean existsByTypeAndNameAndIdNot(CategoryType type, String name, Long id);
    
    // 조회
    Optional<Category> findByTypeAndName(CategoryType type, String name);

}
