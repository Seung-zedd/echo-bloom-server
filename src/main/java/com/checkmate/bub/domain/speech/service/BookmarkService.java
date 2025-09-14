package com.checkmate.bub.domain.speech.service;

import com.checkmate.bub.domain.bookmark.domain.BookmarkEntity;
import com.checkmate.bub.domain.speech.repository.BookmarkRepository;
import com.checkmate.bub.global.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * UR-USER-021, UR-USER-022, AR-ADMIN-005: 북마크 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookmarkService {
    
    private final BookmarkRepository bookmarkRepository;
    
    /**
     * UR-USER-021: 확언 북마크 추가
     * AR-ADMIN-005: 개인화된 문장 저장
     */
    @Transactional
    public BookmarkEntity addBookmark(String sentence, String tone) {
        String userNickname = SecurityUtils.getCurrentNickname();
        
        boolean alreadyBookmarked = bookmarkRepository.existsByUserNicknameAndSentence(userNickname, sentence);
        if (alreadyBookmarked) {
            throw new IllegalArgumentException("이미 북마크에 추가된 문장입니다.");
        }
        
        BookmarkEntity bookmark = BookmarkEntity.builder()
                .userNickname(userNickname)
                .sentence(sentence)
                .tone(tone)
                .build();
        
        return bookmarkRepository.save(bookmark);
    }
    
    /**
     * UR-USER-022: 확언 북마크 제거
     * AR-ADMIN-005: 개인화된 문장 관리
     */
    @Transactional
    public void removeBookmark(String sentence) {
        String userNickname = SecurityUtils.getCurrentNickname();
        
        boolean exists = bookmarkRepository.existsByUserNicknameAndSentence(userNickname, sentence);
        if (!exists) {
            throw new IllegalArgumentException("북마크에 존재하지 않는 문장입니다.");
        }
        
        bookmarkRepository.deleteByUserNicknameAndSentence(userNickname, sentence);
    }
    
    /**
     * 사용자의 모든 북마크 조회
     */
    public List<BookmarkEntity> getUserBookmarks() {
        String userNickname = SecurityUtils.getCurrentNickname();
        return bookmarkRepository.findByUserNicknameOrderByCreatedAtDesc(userNickname);
    }
    
    /**
     * 특정 문장이 북마크되어 있는지 확인
     */
    public boolean isBookmarked(String sentence) {
        String userNickname = SecurityUtils.getCurrentNickname();
        return bookmarkRepository.existsByUserNicknameAndSentence(userNickname, sentence);
    }
}