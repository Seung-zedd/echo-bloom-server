package com.checkmate.bub.speech.controller;

import com.checkmate.bub.speech.dto.BookmarkRequestDto;
import com.checkmate.bub.speech.dto.BookmarkResponseDto;
import com.checkmate.bub.speech.domain.BookmarkEntity;
import com.checkmate.bub.speech.service.BookmarkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

import java.util.List;

/**
 * UR-USER-021, UR-USER-022, AR-ADMIN-005: 북마크 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/bookmarks")
@RequiredArgsConstructor
@Slf4j
public class BookmarkController {
    
    private final BookmarkService bookmarkService;
    
    /**
     * UR-USER-021: 확언 북마크 추가
     * AR-ADMIN-005: 개인화된 문장 저장
     */
    @PostMapping("/add")
    public ResponseEntity<BookmarkResponseDto> addBookmark(@Valid @RequestBody BookmarkRequestDto request) {
        try {
            log.info("북마크 추가 요청 - 문장: {}", request.getSentence());
            
            BookmarkEntity bookmark = bookmarkService.addBookmark(
                    request.getSentence(), 
                    request.getTone());
            
            BookmarkResponseDto response = BookmarkResponseDto.of(
                    bookmark.getId(),
                    bookmark.getSentence(),
                    bookmark.getTone(),
                    bookmark.getCreatedAt(),
                    true
            );
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("북마크 추가 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("북마크 추가 오류", e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * UR-USER-022: 확언 북마크 제거
     * AR-ADMIN-005: 개인화된 문장 관리
     */
    @DeleteMapping("/remove")
    public ResponseEntity<Void> removeBookmark(@RequestParam("sentence") String sentence) {
        try {
            log.info("북마크 제거 요청 - 문장: {}", sentence);
            
            bookmarkService.removeBookmark(sentence);
            return ResponseEntity.ok().build();
            
        } catch (IllegalArgumentException e) {
            log.warn("북마크 제거 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("북마크 제거 오류", e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * 사용자의 모든 북마크 조회
     */
    @GetMapping
    public ResponseEntity<List<BookmarkResponseDto>> getUserBookmarks() {
        try {
            List<BookmarkResponseDto> bookmarks = bookmarkService.getUserBookmarks()
                    .stream()
                    .map(entity -> BookmarkResponseDto.of(
                            entity.getId(),
                            entity.getSentence(),
                            entity.getTone(),
                            entity.getCreatedAt(),
                            true
                    ))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(bookmarks);
        } catch (Exception e) {
            log.error("북마크 조회 오류", e);
            return ResponseEntity.status(500).build();
        }
    }


    /**
     * 특정 문장의 북마크 상태 확인
     */
    @GetMapping("/check")
    public ResponseEntity<BookmarkResponseDto> isBookmarked(@RequestParam("sentence") String sentence) {
        try {
            boolean bookmarked = bookmarkService.isBookmarked(sentence);
            BookmarkResponseDto response = BookmarkResponseDto.ofBookmarkStatus(bookmarked);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("북마크 상태 확인 오류", e);
            return ResponseEntity.status(500).build();
        }
    }
}