package com.checkmate.bub.domain.profile.member.service;

import com.checkmate.bub.domain.bookmark.domain.Bookmark;
import com.checkmate.bub.domain.bookmark.repository.BookmarkRepository;
import com.checkmate.bub.domain.bridge.domain.UserCategoryBridge;
import com.checkmate.bub.domain.bridge.repository.UserCategoryBridgeRepository;
import com.checkmate.bub.domain.category.constant.CategoryType;
import com.checkmate.bub.domain.custom.domain.CustomSentence;
import com.checkmate.bub.domain.custom.repository.CustomSentenceRepository;
import com.checkmate.bub.domain.profile.member.domain.User;
import com.checkmate.bub.domain.profile.member.dto.CategorySelectionDto;
import com.checkmate.bub.domain.profile.member.repository.UserRepository;
import com.checkmate.bub.domain.profile.member.service.helper.UserCategoryBridgeHelper;
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
    private final BookmarkRepository bookmarkRepository;
    private final CustomSentenceRepository customSentenceRepository;

    public List<UserCategoryBridge> getUserCategories(Long userId) {
        return userCategoryBridgeRepository.findByUserId(userId);
    }

    public List<CategorySelectionDto> getUserProblems(Long userId) {
        return userCategoryBridgeRepository.findByUserIdAndCategoryType(userId, CategoryType.PROBLEM)
                .stream()
                .map(bridge -> new CategorySelectionDto(bridge.getCategory().getId(), bridge.getCategory().getName()))
                .toList();
    }

    public CategorySelectionDto getUserTone(Long userId) {
        return userCategoryBridgeRepository.findByUserIdAndCategoryType(userId, CategoryType.TONE)
                .stream()
                .findFirst()
                .map(bridge -> new CategorySelectionDto(bridge.getCategory().getId(), bridge.getCategory().getName()))
                .orElse(null);
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

        String userNickname = user.getNickname();

        // 사용자와 연관된 모든 데이터 삭제 (순서 중요: 외래키 제약조건 고려)
        // 1. UserCategoryBridge 삭제 (userId로 조회)
        userCategoryBridgeRepository.deleteByUserId(userId);

        // 2. Bookmarks 삭제 (userNickname으로 조회)
        List<Bookmark> bookmarks = bookmarkRepository.findByUserNicknameOrderByCreatedAtDesc(userNickname);
        if (!bookmarks.isEmpty()) {
            bookmarkRepository.deleteAll(bookmarks);
        }

        // 3. Custom Sentences 삭제 (userNickname으로 조회)
        List<CustomSentence> customSentences = customSentenceRepository.findByUserNicknameOrderByCreatedAtDesc(userNickname);
        if (!customSentences.isEmpty()) {
            customSentenceRepository.deleteAll(customSentences);
        }

        // 4. 사용자 삭제 (마지막에 수행)
        userRepository.delete(user);
    }
}
