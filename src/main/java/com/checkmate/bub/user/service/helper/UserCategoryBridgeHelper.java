package com.checkmate.bub.user.service.helper;

import com.checkmate.bub.bridge.domain.UserCategoryBridge;
import com.checkmate.bub.bridge.repository.UserCategoryBridgeRepository;
import com.checkmate.bub.category.constant.CategoryType;
import com.checkmate.bub.category.domain.Category;
import com.checkmate.bub.category.repository.CategoryRepository;
import com.checkmate.bub.user.domain.User;
import com.checkmate.bub.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCategoryBridgeHelper {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final UserCategoryBridgeRepository userCategoryBridgeRepository;

    /**
     * Save user's selected categories (problems/tones)
     * Deletes existing selections and replaces with new ones
     *
     * @param userId User ID
     * @param categoryIds Selected category ID list
     * @param expectedType Expected category type (PROBLEM or TONE)
     */
    @Transactional
    public void saveSelections(Long userId, List<Long> categoryIds, CategoryType expectedType) {
        log.info("Starting category selection save. userId: {}, categoryIds: {}, type: {}", userId, categoryIds, expectedType);

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found. userId: " + userId));

        // Delete existing selections
        List<UserCategoryBridge> existingSelections = userCategoryBridgeRepository
                .findByUserIdAndCategoryType(userId, expectedType);
        if (!existingSelections.isEmpty()) {
            userCategoryBridgeRepository.deleteAll(existingSelections);
            log.info("Deleted {} existing selections (type: {})", existingSelections.size(), expectedType);
        }

        // Save new selections
        for (Long categoryId : categoryIds) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new EntityNotFoundException("Category not found. categoryId: " + categoryId));

            if (category.getType() != expectedType) {
                throw new IllegalArgumentException("Category type mismatch. expected: " + expectedType + ", categoryId: " + categoryId);
            }

            UserCategoryBridge bridge = new UserCategoryBridge(user, category);
            userCategoryBridgeRepository.save(bridge);
            log.info("Saved selection: {} -> {} (type: {})", userId, category.getName(), expectedType);
        }

        log.info("Category selection save completed. userId: {}, saved count: {}, type: {}", userId, categoryIds.size(), expectedType);
    }

    /**
     * Save single tone selection by name
     * Used for tone selection during onboarding
     */
    @Transactional
    public void saveToneByName(Long userId, String toneName) {
        log.info("Starting tone selection save by name. userId: {}, toneName: {}", userId, toneName);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found. userId: " + userId));

        // Find tone category by name
        Category toneCategory = categoryRepository.findByTypeAndName(CategoryType.TONE, toneName)
                .orElseThrow(() -> new EntityNotFoundException("Tone category not found. toneName: " + toneName));

        // Delete existing tone selections
        List<UserCategoryBridge> existingToneSelections = userCategoryBridgeRepository
                .findByUserIdAndCategoryType(userId, CategoryType.TONE);
        if (!existingToneSelections.isEmpty()) {
            userCategoryBridgeRepository.deleteAll(existingToneSelections);
            log.info("Deleted {} existing tone selections", existingToneSelections.size());
        }

        // Save new tone selection
        UserCategoryBridge bridge = new UserCategoryBridge(user, toneCategory);
        userCategoryBridgeRepository.save(bridge);

        log.info("Tone selection save completed. userId: {}, toneName: {}", userId, toneName);
    }
}