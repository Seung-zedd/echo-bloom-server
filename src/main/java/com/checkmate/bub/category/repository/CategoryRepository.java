package com.checkmate.bub.category.repository;

import com.checkmate.bub.category.constant.CategoryType;
import com.checkmate.bub.category.domain.Category;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.Optional;


//* /Tone-Examples End Point where AffirmationService stored three different tones with toneExampleResponseDto (related to modifying tones in mypage > sentence invoked by 'updateUserCategories' method of UserController)
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // 중복 방지
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})  // 락 타임아웃 3초 설정 (밀리초 단위)
    boolean existsByTypeAndName(CategoryType type, String name);
//    boolean existsByTypeAndNameAndIdNot(CategoryType type, String name, Long id);
    
    // 조회
    Optional<Category> findByTypeAndName(CategoryType type, String name);

}
