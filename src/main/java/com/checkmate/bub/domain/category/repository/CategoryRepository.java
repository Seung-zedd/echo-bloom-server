package com.checkmate.bub.domain.category.repository;

import com.checkmate.bub.domain.category.constant.CategoryType;
import com.checkmate.bub.domain.category.domain.Category;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.List;
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
    
    // 패턴으로 시작하는 카테고리 조회 (톤 예시 정리용)
    List<Category> findByTypeAndNameStartingWith(CategoryType type, String namePrefix);

}
