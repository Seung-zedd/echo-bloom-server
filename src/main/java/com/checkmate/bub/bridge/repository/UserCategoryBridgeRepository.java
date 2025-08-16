package com.checkmate.bub.bridge.repository;

import com.checkmate.bub.bridge.domain.UserCategoryBridge;
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
}