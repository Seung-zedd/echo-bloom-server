package com.checkmate.bub.category.repository;

import com.checkmate.bub.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
