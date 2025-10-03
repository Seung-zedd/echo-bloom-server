package com.checkmate.bub.domain.bookmark.repository;

import com.checkmate.bub.domain.bookmark.domain.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UR-USER-021, UR-USER-022, AR-ADMIN-005: 북마크 Repository
 * 사용자의 북마크된 문장을 관리
 */
@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    
    // 특정 사용자의 북마크 목록 조회 (최신순)
    List<Bookmark> findByUserNicknameOrderByCreatedAtDesc(String userNickname);
    
    // 특정 사용자의 특정 문장 북마크 조회
    Optional<Bookmark> findByUserNicknameAndSentence(String userNickname, String sentence);
    
    // 북마크 존재 여부 확인
    boolean existsByUserNicknameAndSentence(String userNickname, String sentence);
    
    // 특정 사용자의 북마크 삭제
    void deleteByUserNicknameAndSentence(String userNickname, String sentence);
}
