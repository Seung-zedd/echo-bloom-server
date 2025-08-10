package com.checkmate.bub.category.repository;

import com.checkmate.bub.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CategoryRepository extends JpaRepository<Category, Long> {
    // 중복 방지
    boolean existsByTypeAndName(String type, String name);
    boolean existsByTypeAndNameAndIdNot(String type, String name, Long id);


}
