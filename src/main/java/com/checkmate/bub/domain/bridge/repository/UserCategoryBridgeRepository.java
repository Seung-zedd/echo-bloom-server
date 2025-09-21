package com.checkmate.bub.domain.bridge.repository;

import com.checkmate.bub.domain.bridge.domain.UserCategoryBridge;
import com.checkmate.bub.domain.category.constant.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserCategoryBridgeRepository extends JpaRepository<UserCategoryBridge, Long> {

    @Query("SELECT ucb FROM UserCategoryBridge ucb WHERE ucb.user.id = :userId")
    List<UserCategoryBridge> findByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM UserCategoryBridge ucb WHERE ucb.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
    
    @Query("SELECT ucb FROM UserCategoryBridge ucb WHERE ucb.user.id = :userId AND ucb.category.type = :categoryType")
    List<UserCategoryBridge> findByUserIdAndCategoryType(@Param("userId") Long userId, @Param("categoryType") CategoryType categoryType);

    boolean existsByUserIdAndCategoryType(Long userId, CategoryType category);
    
    @Query("SELECT COUNT(ucb) > 0 FROM UserCategoryBridge ucb WHERE ucb.user.id = :userId AND ucb.category.id = :categoryId")
    boolean existsByUserIdAndCategoryId(@Param("userId") Long userId, @Param("categoryId") Long categoryId);
}