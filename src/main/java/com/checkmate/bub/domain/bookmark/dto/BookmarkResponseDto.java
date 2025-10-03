package com.checkmate.bub.domain.bookmark.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * UR-USER-021, UR-USER-022, UR-USER-027: 북마크 응답 DTO
 * 사용자의 북마크된 확언 정보를 반환하는 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookmarkResponseDto {
    
    /**
     * 북마크 ID (고유 식별자)
     */
    private Long id;
    
    /**
     * 북마크된 확언 문장
     */
    private String sentence;
    
    /**
     * 확언의 톤 (예: 자신감, 사랑, 성취 등)
     */
    private String tone;
    
    /**
     * 북마크 생성 시간
     */
    private LocalDateTime createdAt;
    
    /**
     * 북마크 상태 (true: 북마크됨, false: 북마크 해제됨)
     * UR-USER-022: 북마크 토글 기능을 위한 필드
     */
    private boolean isBookmarked;
    
    /**
     * 북마크 상태만 포함한 생성자 (북마크 확인 API용)
     */
    public static BookmarkResponseDto ofBookmarkStatus(boolean isBookmarked) {
        return BookmarkResponseDto.builder()
                .isBookmarked(isBookmarked)
                .build();
    }
    
    /**
     * 북마크 추가/제거 후 응답용 생성자
     */
    public static BookmarkResponseDto of(Long id, String sentence, String tone, LocalDateTime createdAt, boolean isBookmarked) {
        return BookmarkResponseDto.builder()
                .id(id)
                .sentence(sentence)
                .tone(tone)
                .createdAt(createdAt)
                .isBookmarked(isBookmarked)
                .build();
    }
}
