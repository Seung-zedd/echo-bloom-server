package com.checkmate.bub.user.service;

import com.checkmate.bub.bridge.domain.UserCategoryBridge;
import com.checkmate.bub.bridge.repository.UserCategoryBridgeRepository;
import com.checkmate.bub.category.domain.Category;
import com.checkmate.bub.category.repository.CategoryRepository;
import com.checkmate.bub.user.domain.User;
import com.checkmate.bub.user.dto.UserCategoryRequestDto;
import com.checkmate.bub.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final UserCategoryBridgeRepository userCategoryBridgeRepository;

    @Transactional
    public void updateUserCategories(Long userId, UserCategoryRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Set<Long> distinctIds = new HashSet<>(requestDto.getCategoryIds());
        List<Category> categories = categoryRepository.findAllById(distinctIds);

        if (categories.size() != distinctIds.size()) {
            throw new IllegalArgumentException("존재하지 않는 카테고리가 포함되어 있습니다.");
        }

        userCategoryBridgeRepository.deleteByUserId(userId);

        List<UserCategoryBridge> bridges = categories.stream()
                .map(category -> new UserCategoryBridge(user, category))
                .toList();

        userCategoryBridgeRepository.saveAll(bridges);
    }
}