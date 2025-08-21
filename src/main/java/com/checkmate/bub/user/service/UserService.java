package com.checkmate.bub.user.service;

import com.checkmate.bub.bridge.domain.UserCategoryBridge;
import com.checkmate.bub.bridge.repository.UserCategoryBridgeRepository;
import com.checkmate.bub.category.constant.CategoryType;
import com.checkmate.bub.user.domain.User;
import com.checkmate.bub.user.repository.UserRepository;
import com.checkmate.bub.user.service.helper.UserCategoryBridgeHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserCategoryBridgeRepository userCategoryBridgeRepository;
    private final UserCategoryBridgeHelper userCategoryBridgeHelper;

    public List<UserCategoryBridge> getUserCategories(Long userId) {
        return userCategoryBridgeRepository.findByUserId(userId);
    }

    // Get user nickname by ID (for JavaScript display)
    public String getUserNickname(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        return user.getNickname();
    }

    // Update user's selected problems (my-page)
    @Transactional
    public void updateUserProblems(Long userId, List<Long> problemIds) {
        userCategoryBridgeHelper.saveSelections(userId, problemIds, CategoryType.PROBLEM);
    }

    // Get user's selected tones (my-page)
    public List<UserCategoryBridge> getUserTones(Long userId) {
        return userCategoryBridgeRepository.findByUserIdAndCategoryType(userId, CategoryType.TONE);
    }

    // Update user's selected tone (my-page)
    @Transactional
    public void updateUserTone(Long userId, String toneName) {
        userCategoryBridgeHelper.saveToneByName(userId, toneName);
    }

    @Transactional
    public void withdrawUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
                
        // 사용자와 연관된 데이터 삭제
        userCategoryBridgeRepository.deleteByUserId(userId);
        
        // 사용자 삭제
        userRepository.delete(user);
    }
}